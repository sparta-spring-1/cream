package com.sparta.cream.domain.trade.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 거래 체결 프로세스를 총괄하는 서비스입니다.
 * 본 서비스는 매칭 엔진의 진입점 역할을 하며,
 * 분산 락 제어, 비동기 처리, 배치 매칭, 거래 취소 등
 * 거래 흐름 전반의 오케스트레이션을 담당합니다.
 * 실제 매칭 알고리즘과 체결 로직은 {@link MatchingService}에 위임하며,
 * 본 서비스는 동시성 제어와 호출 순서 보장에 집중합니다.
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
	private final RedissonClient redissonClient;
	private final MatchingService matchingService;

	/**
	 * 입찰 체결 프로세스를 별도의 스레드에서 비동기적으로 시작합니다.
	 * 시에 여러 스레드에서 동일 입찰을 처리하려는 경우
	 * 낙관적 락 충돌이 발생할 수 있으며,
	 * 이 경우 이미 다른 스레드에서 처리 중인 것으로 판단하고
	 * 추가 동작 없이 안전하게 종료합니다.
	 *
	 * @param bidId 매칭을 진행할 대상 입찰 ID
	 */
	@Async("taskExecutor")
	public void handleMatchingInternal(Long bidId) {
		try {
			this.processTradeMatching(bidId);
		} catch (ObjectOptimisticLockingFailureException e) {
			log.info("낙관적 락 충돌: 이미 처리 중인 입찰입니다. Bid ID: {}", bidId);
		} catch (Exception e) {
			log.error("매칭 중 예상치 못한 에러 발생: {}", e.getMessage());
		}
	}

	/**
	 * 실시간 매칭 엔진을 가동하기 위한 사전 검증 및 분산 락을 제어합니다.
	 * 1. 대상 입찰이 여전히 {@link BidStatus#PENDING} 상태인지 DB를 통해 최종 확인합니다.
	 * 2. 동일한 상품 옵션에 대해 여러 스레드가 동시에 접근하지 못하도록 분산 락을 획득합니다.
	 * 3. 락 획득 후, 다른 스레드에 의해 이미 체결되었을 가능성을 배제하기 위해 한 번 더 상태를 점검합니다.
	 * 4. 모든 조건이 충족되면를 호출하여 최적의 상대를 탐색합니다.
	 *
	 * @param bidId 매칭 프로세스를 시작할 기준 입찰 ID
	 */
	public void processTradeMatching(Long bidId) {
		Bid newBid = bidRepository.findById(bidId)
			.filter(bid -> bid.getStatus() == BidStatus.PENDING)
			.orElse(null);

		if (newBid == null)
			return;

		RLock lock = redissonClient.getLock("lock:option:" + newBid.getProductOption().getId());
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				matchingService.checkStatusAndMatch(bidId);
			} else {
				log.warn("매칭 락 획득 실패: Bid ID {}", bidId);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("매칭 중 인터럽트 발생: {}", e.getMessage());
		} finally {
			if (lock.isHeldByCurrentThread())
				lock.unlock();
		}
	}

	/**
	 * 모든 대기 중인 구매입찰을 대상으로 배치 매칭을 수행합니다.
	 * 상품 옵션 단위로 입찰을 그룹화한 뒤,
	 * 각 옵션별로 분산락을 획득하여 순차적으로 매칭을 시도합니다.
	 * 대량의 입찰을 처리하는 배치 상황에서도 옵션단위의 원자성을 보장하는 구조입니다.
	 */
	public void matchAllPendingBids() {
		List<Bid> pendingBuyBids = bidRepository.findByTypeAndStatusOrderByCreatedAtAsc(BidType.BUY, BidStatus.PENDING);

		Map<Long, List<Bid>> bidsByOption = pendingBuyBids.stream()
			.collect(Collectors.groupingBy(bid -> bid.getProductOption().getId()));

		bidsByOption.forEach((optionId, bids) -> {
			RLock lock = redissonClient.getLock("lock:option:" + optionId);
			try {
				if (lock.tryLock(10, 20, TimeUnit.SECONDS)) {
					for (Bid buyBid : bids) {
						matchingService.checkStatusAndMatch(buyBid.getId());
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				if (lock.isHeldByCurrentThread()) lock.unlock();
			}
		});
	}

	/**
	 * 분산 락을 사용하여 안전하게 거래 취소를 처리합니다.
	 * 1. 동일한 거래({@code tradeId})에 대해 여러 번의 취소 요청이 동시에 들어올 경우를 대비해 전용 락을 획득합니다.
	 * 2. 락 획득 실패 시, 중복 요청이나 서버 부하로 판단하여 {@link ErrorCode#LOCK_ACQUISITION_FAILED} 예외를 발생시킵니다.
	 * 3. 안전하게 락을 획득한 후에는 실제 취소 비즈니스 로직({@link #cancelTrade(Long, Long)})을 실행합니다.
	 * 4. 락의 대기 시간은 5초, 점유 시간은 3초로 설정하여 불필요한 리소스 점유를 방지합니다.
	 *
	 * @param tradeId 취소할 거래의 고유 식별자
	 * @param requestUserId 취소를 요청한 사용자의 ID
	 */
	public void cancelTradeWithLock(Long tradeId, Long requestUserId) {
		RLock lock = redissonClient.getLock("lock:trade:" + tradeId);
		try {
			if (!lock.tryLock(5, 3, TimeUnit.SECONDS)) {
				throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
			}
			this.cancelTrade(tradeId, requestUserId);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		} finally {
			if (lock.isHeldByCurrentThread())
				lock.unlock();
		}
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
	 * 특정 거래의 상세 내역을 데이터베이스에서 조회합니다.
	 * {@code readOnly = true} 설정을 통해 트랜잭션 오버헤드를 줄이고 조회 성능을 최적화했습니다.
	 * 만약 전달받은 식별자에 해당하는 거래 정보가 존재하지 않을 경우,
	 * 시스템에서 정의한 비즈니스 예외({@link ErrorCode#RESOURCE_NOT_FOUND})를 발생시킵니다.
	 *
	 * @param tradeId 조회하고자 하는 거래의 고유 식별자(ID)
	 * @return 조회된 {@link Trade} 엔티티 객체
	 * @throws BusinessException 거래 정보가 존재하지 않을 경우 발생 (404 Not Found)
	 */
	@Transactional(readOnly = true)
	public Trade findById(Long tradeId) {
		return tradeRepository.findById(tradeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
	}


}
