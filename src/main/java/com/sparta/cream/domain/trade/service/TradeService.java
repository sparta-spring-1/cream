package com.sparta.cream.domain.trade.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.entity.TradeStatus;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BidErrorCode;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 거래 체결 서비스
 * 구매와 판매 입찰을 매칭하여 실제 거래(Trade)를 생성하는 핵심 비즈니스 로직을 수행합니다.
 * 실시간 매칭과 배치성 전체 매칭 기능을 제공합니다.
 * TradeService.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@Service
@RequiredArgsConstructor
public class TradeService {
	private final BidRepository bidRepository;
	private final TradeRepository tradeRepository;
	private final NotificationService notificationService;

	/**
	 * 단일 입찰에 대한 즉시 매칭을 시도합니다.
	 * 새로운 입찰이 등록될 때 호출되어 반대 타입의 최적 입찰(최저 판매가 또는 최고 구매가)를 찾아 체결합니다.
	 *
	 * @param newBid 새로 등록된 구매 또는 판매 입찰 객체
	 */
	@Transactional
	public void processMatching(Bid newBid) {
		Optional<Bid> matchedBid;

		if (newBid.getType() == BidType.BUY) {
			matchedBid = bidRepository.findMatchingSellBids(
				newBid.getProductOption().getId(),
				newBid.getPrice(),
				newBid.getUser().getId(),
				PageRequest.of(0, 1)
			).stream().findFirst();
		} else {
			matchedBid = bidRepository.findMatchingBuyBids(
				newBid.getProductOption().getId(),
				newBid.getPrice(),
				newBid.getUser().getId(),
				PageRequest.of(0, 1)
			).stream().findFirst();
		}

		executeTrade(newBid, matchedBid);
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

		if (trade.getStatus() == TradeStatus.PAYMENT_CANCELED) {
			throw new BusinessException(BidErrorCode.ALREADY_CANCELED_TRADE);
		}

		if (trade.getStatus() == TradeStatus.PAYMENT_COMPLETED) {
			throw new BusinessException(BidErrorCode.CANNOT_CANCEL_TRADE);
		}


		Bid purchaseBid = trade.getPurchaseBidId();
		Bid saleBid = trade.getSaleBidId();

		boolean isBuyer = purchaseBid.getUser().getId().equals(requestUserId);
		boolean isSeller = saleBid.getUser().getId().equals(requestUserId);

		if (!isBuyer && !isSeller) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		Bid cancelBid = isBuyer ? purchaseBid : saleBid;
		Bid victimBid = isBuyer ? saleBid : purchaseBid;

		trade.cancelPayment();
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
