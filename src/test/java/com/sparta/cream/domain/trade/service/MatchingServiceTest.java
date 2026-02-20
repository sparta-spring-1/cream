package com.sparta.cream.domain.trade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;

/**
 * MatchingService 에 대한 핵심 매칭 엔진 로직 테스트 클래스입니다.
 * 1. Redis 후보 탐색 로직
 * 2. 가격 일치 여부 검증
 * 3. 최종 Trade 생성 및 Bid 상태 변경 검증
 * MatchingServiceTest.java
 *
 * @author kimsehyun
 * @since 2026. 2. 11.
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

	@InjectMocks
	private MatchingService matchingService;

	@Mock
	private BidRepository bidRepository;

	@Mock
	private TradeRepository tradeRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RScoredSortedSet<Long> candidateSet;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	/**
	 * 가격 조건이 충족될 때 체결 프로세스가 완벽히 수행되는지 검증합니다.
	 * 구매/판매 Bid 상태가 MATCHED 로 변경되는지
	 * rade 엔티티가 정상적으로 저장되는지
	 */
	@Test
	@DisplayName("구매가와 판매가가 일치하면 체결이 성공하고 Trade가 저장된다")
	void matchSuccessTest() {
		// given
		Long buyBidId = 1L;
		Long sellBidId = 2L;
		ProductOption option = createOption(4L);

		Bid buyBid = createBid(buyBidId, option, 1L, 250000L, BidType.BUY);
		Bid sellBid = createBid(sellBidId, option, 2L, 250000L, BidType.SELL);

		when(bidRepository.findById(buyBidId)).thenReturn(Optional.of(buyBid));


		when(bidRepository.findAllById(anyList())).thenReturn(List.of(buyBid, sellBid));

		doReturn(candidateSet).when(redissonClient).getScoredSortedSet(anyString());
		when(candidateSet.first()).thenReturn(sellBidId);
		when(candidateSet.getScore(sellBidId)).thenReturn(250000.0);
		when(tradeRepository.save(any(Trade.class)))
			.thenAnswer(invocation -> {
				Trade trade = invocation.getArgument(0);
				ReflectionTestUtils.setField(trade, "id", 100L);
				return trade;
			});

		// when
		matchingService.checkStatusAndMatch(buyBidId);

		// then
		assertEquals(BidStatus.MATCHED, buyBid.getStatus());
		assertEquals(BidStatus.MATCHED, sellBid.getStatus());
		verify(tradeRepository, times(1)).save(any(Trade.class));
	}

	/**
	 * 가격이 맞지 않을 경우 루프가 중단되는지 검증합니다.
	 * 구매 Bid 상태가 PENDING 상태로 유지되는지
	 * Trade 저장 로직이 호출되지 않는지
	 */
	@Test
	@DisplayName("구매가가 판매가보다 낮으면 매칭을 중단한다")
	void matchFailByPriceTest() {
		// given
		Long buyBidId = 1L;
		Long sellBidId = 2L;
		Bid buyBid = createBid(buyBidId, createOption(4L), 1L, 200000L, BidType.BUY);

		doReturn(candidateSet).when(redissonClient).getScoredSortedSet(anyString());
		when(candidateSet.first()).thenReturn(sellBidId);
		when(candidateSet.getScore(sellBidId)).thenReturn(250000.0);
		when(bidRepository.findById(buyBidId)).thenReturn(Optional.of(buyBid));

		// when
		matchingService.checkStatusAndMatch(buyBidId);

		// then
		assertEquals(BidStatus.PENDING, buyBid.getStatus());
		verify(tradeRepository, never()).save(any());
	}

	/**
	 * Redis에는 있었지만 DB 상태가 이미 MATCHED인 경우
	 * 이미 체결된 입찰이 Redis 후보군에서 제거되는지
	 * Trade 가 생성되지 않고 매칭이 종료되는지
	 */
	@Test
	@DisplayName("매칭 대상이 이미 체결된 상태라면 Redis에서 제거하고 다음 후보를 찾는다")
	void matchFailByAlreadyMatchedTarget() {
		// given
		Long buyBidId = 1L;
		Long dirtyBidId = 99L;

		Bid buyBid = createBid(buyBidId, createOption(4L), 1L, 300000L, BidType.BUY);
		Bid dirtyBid = createBid(dirtyBidId, createOption(4L), 2L, 300000L, BidType.SELL);
		ReflectionTestUtils.setField(dirtyBid, "status", BidStatus.MATCHED);

		doReturn(candidateSet).when(redissonClient).getScoredSortedSet(anyString());
		when(candidateSet.first()).thenReturn(dirtyBidId).thenReturn(null);
		when(candidateSet.getScore(dirtyBidId)).thenReturn(300000.0);

		when(bidRepository.findById(buyBidId)).thenReturn(Optional.of(buyBid));
		when(bidRepository.findAllById(anyList())).thenReturn(List.of(buyBid, dirtyBid));

		// when
		matchingService.checkStatusAndMatch(buyBidId);

		// then
		verify(candidateSet).remove(dirtyBidId);
		verify(tradeRepository, never()).save(any());
	}

	/**
	 * 테스트용 ProductOption 객체를 생성합니다
	 * @param id ProductOption ID
	 * @return 테스트용 ProductOption
	 */
	private ProductOption createOption(Long id) {
		try {
			java.lang.reflect.Constructor<ProductOption> constructor =
				ProductOption.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			ProductOption option = constructor.newInstance();

			ReflectionTestUtils.setField(option, "id", id);

			return option;
		} catch (Exception e) {
			throw new RuntimeException("테스트용 ProductOption 생성 실패", e);
		}
	}

	/**
	 * 테스트용 Bid 엔티티를 생성합니다.
	 * 사용자, 상품 옵션, 가격, 타입, 상태를 지정하여
	 * 다양한 매칭 시나리오를 구성할 수 있도록 설계되었습니다.
	 * @param id Bid ID
	 * @param option 상품 옵션
	 * @param userId 사용자 ID
	 * @param price 입찰 가격
	 * @param type 입찰 타입
	 * @return 테스트용 Bid 엔티티
	 */
	private Bid createBid(Long id, ProductOption option, Long userId, Long price, BidType type) {
		Users user = new Users("user" + userId + "@test.com", "pw", "nick", "010", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);

		Bid bid = Bid.builder()
			.user(user)
			.productOption(option)
			.price(price)
			.type(type)
			.status(BidStatus.PENDING)
			.build();
		ReflectionTestUtils.setField(bid, "id", id);
		return bid;
	}
}
