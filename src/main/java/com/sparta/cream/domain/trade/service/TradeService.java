package com.sparta.cream.domain.trade.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 거래 체결 서비스
 * 구매와 판매 입찰을 매칭하여 실제 거래(Trade)를 생성하는 핵심 비즈니스 로직을 수행합니다.
 * 실시간 매칭과 배치성 전체 매칭 기능을 제공합니다.
 * TradeService.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {
	private final BidRepository bidRepository;
	private final TradeRepository tradeRepository;
	private final NotificationService notificationService;

	/**
	 * 입찰 체결 프로세스를 비동기 스레드에서 실행하는 진입점 메서드입니다.
	 * 입찰 등록(또는 수정) 트랜잭션이 성공적으로 커밋된 후 호출되며,
	 * 실제 매칭 로직을 별도의 스레드 풀에서 실행함으로써 사용자 응답 시간을 최적화합니다.
	 */
	@Async
	@Transactional
	public void processMatching(Long bidId) {
		try {
			handleMatchingInternal(bidId);
		} catch (Exception e) {
			log.error("매칭 엔진 실행 중 오류 발생 (입찰 ID: {}): {}", bidId, e.getMessage());
		}
	}

	/**
	 * 비동기 환경에서 단일 입찰에 대한 실제 매칭 체결 프로세스를 처리합니다.
	 * processMatching }에 의해 비동기적으로 호출되며,
	 * 데이터 정합성을 위해 최신 입찰 상태 재확인 및 비관적 락을 사용합니다.
	 * 1.전달받은 ID로 입찰의 최신 상태를 DB에서 조회
	 * 2.입찰상태가 PENDING아닌 즉시 종료
	 * 3.반대 타입의 입찰 중 가격조건이 맞는 후보 조회
	 * 4.매칭 후보가 존재ㅐ할 경우 호출하여 해당 레코드에 쓰기 잠금 획득
	 * 5.락 획득후 최종적으로 executeTrade 통해 거래 체결실행
	 * @param bidId 체결을 시도할 신규 입찰 식별자
	 */
	@Transactional
	public void handleMatchingInternal(Long bidId) {
		Bid newBid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		if (newBid.getStatus() != BidStatus.PENDING) return;

		Optional<Bid> matchedBid;

		if (newBid.getType() == BidType.BUY) {
			matchedBid = bidRepository.findMatchingSellBids(
				newBid.getProductOption().getId(), newBid.getPrice(),
				newBid.getUser().getId(), PageRequest.of(0, 1)
			).stream().findFirst();
		} else {
			matchedBid = bidRepository.findMatchingBuyBids(
				newBid.getProductOption().getId(), newBid.getPrice(),
				newBid.getUser().getId(), PageRequest.of(0, 1)
			).stream().findFirst();
		}

		matchedBid.ifPresent(candidate -> {
			bidRepository.findByIdForUpdate(candidate.getId())
				.ifPresent(target -> executeTrade(newBid, Optional.of(target)));
		});
	}

	/**
	 * 대기 중인 모든 구매 입찰을 대상으로 일괄 체결을 시도합니다.
	 * 등록 시간 순(ASC)으로 구매 입찰을 순회하며 매칭 가능한 판매 입찰이 있는지 확인합니다.
	 */
	@Transactional
	public void matchAllPendingBids() {
		List<Bid> pendingBuyBids = bidRepository.findByTypeAndStatusOrderByCreatedAtAsc(BidType.BUY, BidStatus.PENDING);

		for (Bid buyBid : pendingBuyBids) {
			if (buyBid.getStatus() != BidStatus.PENDING) continue;

			Optional<Bid> matchedSellBid = bidRepository.findMatchingSellBids(
				buyBid.getProductOption().getId(),
				buyBid.getPrice(),
				buyBid.getUser().getId(),
				PageRequest.of(0, 1)
			).stream().findFirst();

			executeTrade(buyBid, matchedSellBid);
		}
	}

	/**
	 * 두 입찰의 매칭이 확정되었을때 실제 거래래를 실행하고 당사자들에게 알림을 발송합니다
	 * 1. 두입찰 객체의 상태를 (MATCHED)로 변경합니다
	 * 2.거래정보를 생성하고 영속화합니다.
	 * 3.NotificationService를 호출하여 구매자에게는 체결알림을, 판매자에게는 판매완료 및 결제 안내 알림을 각각 발송합니다/
	 *
	 * @param newBid 기준이 되는 입찰 객체
	 * @param matchedBid 매칭된 대상 입찰 객체
	 */
	private void executeTrade(Bid newBid, Optional<Bid> matchedBid) {
		matchedBid.ifPresent(target -> {
			newBid.match();
			target.match();

			Long tradePrice = target.getPrice();

			Bid purchase = newBid.getType() == BidType.BUY ? newBid : target;
			Bid sale = newBid.getType() == BidType.SELL ? newBid : target;

			Trade trade = new Trade(purchase, sale, tradePrice);
			tradeRepository.save(trade);

			notificationService.createNotification(
				purchase.getUser().getId(),
				String.format("축하합니다! %s 상품의 거래가 %d원에 체결되었습니다.",
					purchase.getProductOption().getProduct().getName(), tradePrice)
			);

			notificationService.createNotification(
				sale.getUser().getId(),
				String.format("등록하신 판매 입찰 상품이 결제 완료되어 %d원에 거래가 체결되었습니다.", tradePrice)
			);

			System.out.println("체결 성공!");
			System.out.println("- 구매 입찰 ID: " + trade.getPurchaseBidId().getId());
			System.out.println("- 판매 입찰 ID: " + trade.getSaleBidId().getId());
			System.out.println("- 체결 금액: " + trade.getFinalPrice());
		});
	}

	/**
	 * 체결된 거래를 취소하는 로직입니다.
	 * 구매자 또는 판매자 중 체결 당사자가 거래를 취소할 수 있으며,
	 * 취소로직
	 * 1.요청 사용자가 구매자인지 판매자인지 판단
	 * 2.취소를 요청한 체결은 입찰 상태로 변경
	 * 3.거래 상태를 CANCELED로 변경
	 * 4.취소 요청 사용자에게 입찰 제한 패널티(3일)를 적용
	 * 5.취소 사용자 . 상대방에게 알림을 전송
	 * @param tradeId 취소할 거래의 ID
	 * @param requestUserId 거래 취소를 요청한 사용자 ID
	 */
	@Transactional
	public void cancelTrade(Long tradeId, Long requestUserId) {
		Trade trade = tradeRepository.findById(tradeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		trade.cancelPayment();

		Bid purchaseBid = trade.getPurchaseBidId();
		Bid saleBid = trade.getSaleBidId();

		boolean isBuyer = purchaseBid.getUser().getId().equals(requestUserId);
		boolean isSeller = saleBid.getUser().getId().equals(requestUserId);

		if (!isBuyer && !isSeller) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		Bid cancelBid = isBuyer ? purchaseBid : saleBid;
		Bid victimBid = isBuyer ? saleBid : purchaseBid;

		cancelBid.cancelByTrade();
		victimBid.restoreToPending();

		Users cancelUser = cancelBid.getUser();
		cancelUser.applyBidPenalty();

		notificationService.createNotification(
			cancelUser.getId(),
			"체결을 취소하여 3일간 입찰 등록이 제한됩니다."
		);

		notificationService.createNotification(
			victimBid.getUser().getId(),
			"상대방의 체결 취소로 입찰이 다시 대기 상태로 전환되었습니다."
		);
	}

	/**
	 * 거래 ID로 거래 내역을 조회합니다.
	 *
	 * @param tradeId 조회할 거래 식별자
	 * @return 조회된 {@link Trade} 엔티티
	 */
	public Trade findById(Long tradeId) {
		return tradeRepository.findById(tradeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
	}

}
