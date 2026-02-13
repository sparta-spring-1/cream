package com.sparta.cream.domain.trade.service;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 매칭 엔진의 핵심 로직을 담당하는 서비스입니다.
 * Redis Sorted Set을 기반으로 가격 조건이 가장 유리한 입찰을 탐색하고,
 * 실재 거래를 생성하며, DB와 Redis 상태를 일관되게 갱신합니다.
 * {@link TradeService}에서 분산 락을 획득한 이후 호출되며,
 * 매칭 로직 자체에만 집중하도록 설계되었습니다
 * MatchingService.java
 *
 * @author kimsehyun
 * @since 2026. 2. 11.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

	private final BidRepository bidRepository;
	private final TradeRepository tradeRepository;
	private final NotificationService notificationService;
	private final RedissonClient redissonClient;

	/**
	 * 입찰의 최신 상태를 확인한 후 매칭을 시작합니다.
	 * 분산 락 획득 이후 호출되며, DB 기준으로 입찰이 여전히 {@link BidStatus#PENDING} 상태인 경우에만
	 * 실제 매칭 탐색을 수행합니다.
	 * @param bidId 매칭을 시도할 입찰 ID
	 */
	@Transactional
	public void checkStatusAndMatch(Long bidId) {
		bidRepository.findById(bidId)
			.filter(bid -> bid.getStatus() == BidStatus.PENDING)
			.ifPresent(this::findBestCandidateAndMatch);
	}

	/**
	 * Redis Sorted Set에서 가격 조건이 가장 유리한 매칭 후보를 탐색합니다.
	 * 매 입찰의 경우 가장 낮은 판매가를,
	 * 판매 입찰의 경우 가장 높은 구매가를 우선적으로 조회합니다.
	 * 가격 조건이 맞지 않거나,
	 * DB 반영 과정에서 실패한 후보는 Redis에서 제거한 뒤
	 * 다음 후보를 계속 탐색합니다.
	 * @param newBid 신규로 등록되었거나 수정된 입찰
	 */
	private void findBestCandidateAndMatch(Bid newBid) {
		String targetKey = (newBid.getType() == BidType.BUY) ?
			"bids:sell:" + newBid.getProductOption().getId() :
			"bids:buy:" + newBid.getProductOption().getId();

		RScoredSortedSet<Long> candidateSet = redissonClient.getScoredSortedSet(targetKey);

		while (true) {
			Long matchedBidId = candidateSet.first();
			if (matchedBidId == null) break;

			Double matchedPrice = candidateSet.getScore(matchedBidId);

			if (!isMatchable(newBid, matchedPrice)) {
				break;
			}

			boolean success = matchAndSave(newBid.getId(), matchedBidId);

			if (success) {
				log.info("매칭 성공: [신규 {}] - [대상 {}]", newBid.getId(), matchedBidId);
				break;
			} else {
				candidateSet.remove(matchedBidId);
			}
		}
	}

	/**
	 * 두 입찰의 체결을 확정하고 DB 및 Redis 상태를 원자적으로 갱신합니다.
	 * 1. 전달받은 두 입찰 ID가 유효한지 확인하고, 최신 엔티티 정보를 DB에서 조회합니다.
	 * 2. 두 입찰 모두 여전히 '대기(PENDING)' 상태인 경우에만 실제 체결 처리를 진행합니다.
	 * 3. 입찰 상태를 '체결(MATCHED)'로 변경하고, 거래(Trade) 내역을 생성하여 영속화합니다.
	 * 4. 처리가 완료된 입찰은 Redis 대기열에서 즉시 제거하여 중복 매칭을 방지합니다.
	 * 5. 마지막으로 거래 당사자들에게 체결 완료 알림을 발송합니다.
	 *
	 * @param newBidId 신규 입찰의 고유 식별자
	 * @param matchedBidId 매칭된 상대방 입찰의 고유 식별자
	 * @return 체결 성공 시 true, 상태 부적합 등으로 실패 시 false 반환
	 */
	private boolean matchAndSave(Long newBidId, Long matchedBidId) {
		List<Bid> bids = bidRepository.findAllById(List.of(newBidId, matchedBidId));
		if (bids.size() < 2) return false;

		Bid current = bids.stream().filter(b -> b.getId().equals(newBidId)).findFirst().orElse(null);
		Bid target = bids.stream().filter(b -> b.getId().equals(matchedBidId)).findFirst().orElse(null);

		if (current != null && target != null &&
			current.getStatus() == BidStatus.PENDING && target.getStatus() == BidStatus.PENDING) {

			current.match();
			target.match();

			Long finalPrice = target.getPrice();

			Trade trade = new Trade(
				current.getType() == BidType.BUY ? current : target,
				current.getType() == BidType.SELL ? current : target,
				finalPrice
			);
			tradeRepository.save(trade);

			removeFromZSet(current);
			removeFromZSet(target);

			sendNotifications(current, target, finalPrice);

			return true;
		}
		return false;
	}

	/**
	 * 현재 입찰가와 매칭 후보가 가격 조건에 맞는지 확인합니다.
	 * - 구매 입찰: 내가 제시한 가격보다 판매가가 낮거나 같아야 체결 가능
	 * - 판매 입찰: 내가 제시한 가격보다 구매가가 높거나 같아야 체결 가능
	 *
	 * @param newBid 신규로 등록/수정된 입찰 객체
	 * @param matchedPrice 레디스에서 조회한 상대방 입찰의 가격 (매칭 후보)
	 * @return 체결 가능한 가격 조건이면 true, 아니면 false
	 */
	private boolean isMatchable(Bid newBid, Double matchedPrice) {
		if (newBid.getType() == BidType.BUY) {
			return newBid.getPrice().doubleValue() >= matchedPrice;
		} else {
			return newBid.getPrice().doubleValue() <= matchedPrice;
		}
	}

	/**
	 * 처리가 완료된 입찰을 Redis 정렬 집합(ZSet)에서 제거합니다.
	 * 체결이 성사되었거나 입찰이 취소된 경우, 더 이상 매칭 대상이 되지 않도록
	 * 해당 상품 옵션의 대기열에서 해당 입찰 ID를 삭제합니다.
	 *
	 * @param bid 대기열에서 제거할 입찰 객체
	 */
	private void removeFromZSet(Bid bid) {
		String key = (bid.getType() == BidType.BUY ? "bids:buy:" : "bids:sell:") + bid.getProductOption().getId();
		redissonClient.getScoredSortedSet(key).remove(bid.getId());
	}

	/**
	 * 거래 체결관료 . 거래 당사자들에게 알림을 전송합니다.
	 * 체결된 거래의 금액정보를 포함한 동일한 메시지를
	 * 구매자아 판매자에게 가각 전다뢷ㅂ니다.
	 * 본 메서드는 거래 생성이후 호출되며,
	 * 알림 발송 실패가 매칭 트랜잭션에 영향을 주지 않도록
	 * 별도의 비즈니스 로직을 포함하지 않습니다.
	 *
	 * @param b1 거래에 참여한 첫번째 입찰
	 * @param b2 거래에 참여한 두번째 입찰
	 * @param price 최종 체결 금액
	 */
	private void sendNotifications(Bid b1, Bid b2, Long price) {
		String msg = String.format("거래가 체결되었습니다. (금액: %d원)", price);
		notificationService.createNotification(b1.getUser().getId(), msg);
		notificationService.createNotification(b2.getUser().getId(), msg);
	}
}
