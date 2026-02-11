package com.sparta.cream.domain.trade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.entity.TradeStatus;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;

/**
 * TradeService 에 대한 비즈니스 로직 테스트 클래스입니다.
 * 구매 입찰과 판매 입찰 간의 체결엔진 로직을 검증합니다.
 * Mockito 프레임워크를 사용하여 Repository 의존성을 분리하고 순수 서비스 로직을 테스트합니다.
 * TradeServiceTest.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

	@InjectMocks
	private TradeService tradeService;

	@Mock
	private BidRepository bidRepository;

	@Mock
	private TradeRepository tradeRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private org.redisson.api.RedissonClient redissonClient;

	@Mock
	private org.redisson.api.RScoredSortedSet scoredSortedSet;

	@Mock
	private MatchingService matchingService;

	@Mock
	private org.redisson.api.RLock lock;

	@Mock
	private org.springframework.beans.factory.ObjectProvider<TradeService> selfProvider;

	@BeforeEach
	void setUp() {
		lenient().when(redissonClient.getScoredSortedSet(anyString())).thenReturn(scoredSortedSet);
		lenient().when(redissonClient.getLock(anyString())).thenReturn(lock);
		lenient().when(selfProvider.getIfAvailable()).thenReturn(tradeService);
		lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);
		try {
			lenient().when(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
		} catch (InterruptedException e) {}
	}

	/**
	 * 동일한 상품 옵션에 대해 구매가와 판매자가 일치할 경우 성공케이스
	 * 1. 입찰 상태가 MATCHED 로 변경되는지 확인
	 * 2. 새로운 Trade 엔티티가 생성되어 저장되는지 확인
	 */
	@Test
	@DisplayName("입찰 ID가 주어지면 락을 획득하고 매칭 서비스를 호출한다")
	void matchBidsSuccessTest() throws InterruptedException {
		// given
		Long buyBidId = 1L;
		ProductOption option = createOption(4L);
		Bid buyBid = createBidWithOption(buyBidId, option, 1L, 250000L, BidType.BUY);

		// 상세 조회 모킹
		when(bidRepository.findById(buyBidId)).thenReturn(Optional.of(buyBid));
		// 락 획득 성공 모킹
		when(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);

		// when: matchAllPendingBids 대신 직접 호출 (더 직관적)
		tradeService.processTradeMatching(buyBidId);

		// then
		verify(lock).tryLock(anyLong(), anyLong(), any());
		verify(matchingService).checkStatusAndMatch(buyBidId);
		verify(lock).unlock();
	}

	/**
	 * 입찰 대기열이 비어있을 경우 매칭 엔진을 호출하지 않는지 확인합니다.
	 */
	@Test
	@DisplayName("대기 중인 입찰이 없으면 매칭을 시도하지 않는다")
	void matchBidsNoPendingTest() throws InterruptedException {
		// given
		when(bidRepository.findByTypeAndStatusOrderByCreatedAtAsc(any(), any()))
			.thenReturn(List.of());

		// when
		tradeService.matchAllPendingBids();

		// then
		verify(matchingService, never()).checkStatusAndMatch(anyLong());
		verify(lock, never()).tryLock(anyLong(), anyLong(), any());
	}

	/**
	 * 테스트용 상품 옵션 객체 생성 헬퍼 메서드
	 * Reflection 을 사용하여 protected 기본 생성자를 우회하고 ID를 강제 주입합니다.
	 * @param id 설정할 옵션 ID
	 * @return 생성된 ProductOption 객체
	 */
	private ProductOption createOption(Long id) {
		try {
			com.sparta.cream.entity.Product product = com.sparta.cream.entity.Product.builder()
				.name("임시 상품명")
				.build();

			ReflectionTestUtils.setField(product, "id", 100L);

			java.lang.reflect.Constructor<ProductOption> constructor = ProductOption.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			ProductOption option = constructor.newInstance();

			ReflectionTestUtils.setField(option, "id", id);
			ReflectionTestUtils.setField(option, "product", product);

			return option;
		} catch (Exception e) {
			throw new RuntimeException("테스트용 ProductOption 생성 실패", e);
		}
	}

	/**
	 * 테스트용 입찰 객체 생성 헬퍼 메서드
	 * @param bidId 입찰 식별자
	 * @param option 상품 옵션 객체
	 * @param userId 사용자 식별자
	 * @param price 입찰 가격
	 * @param type 입찰 타입(BUY/SELL)
	 * @return 생성된 Bid 객체
	 */
	private Bid createBidWithOption(Long bidId, ProductOption option, Long userId, Long price, BidType type) {
		Users user = new Users("test" + userId + "@test.com", "pw", "user" + userId, "010-1234-5678", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);

		Bid bid = Bid.builder()
			.user(user)
			.productOption(option)
			.price(price)
			.type(type)
			.status(BidStatus.PENDING)
			.expiresAt(LocalDateTime.now().plusDays(7))
			.build();

		ReflectionTestUtils.setField(bid, "id", bidId);
		return bid;
	}

	/**
	 * 체결된 거래를 구매자가 취소하는 경우 테스트입니다.
	 * 1.Trade 상태가 PAYMENT_CANCELED로 변경된다</li>
	 * 2.매자의 입찰은 CANCELED 상태가 된다</li>
	 * 3.판매자의 입찰은 다시 PENDING 상태로 변경된다</li>
	 * 4.구매자에게 입찰 제한 패널티가 적용된다</li>
	 * 5.구매자와 판매자에게 각각 알림이 발송된다</li>
	 */
	@Test
	@DisplayName("체결된 거래를 구매자가 취소하면 Trade, Bid 상태 변경 + 패널티가 적용된다")
	void cancelTradeByBuyerSuccessTest() {
		// given
		Long tradeId = 10L;
		Long buyerId = 1L;
		Long sellerId = 2L;

		ProductOption option = createOption(1L);

		Bid buyBid = createMatchedBid(1L, buyerId, option, BidType.BUY);
		Bid sellBid = createMatchedBid(2L, sellerId, option, BidType.SELL);

		Trade trade = new Trade(buyBid, sellBid, 250000L);
		ReflectionTestUtils.setField(trade, "id", tradeId);

		when(tradeRepository.findById(tradeId))
			.thenReturn(java.util.Optional.of(trade));

		// when
		tradeService.cancelTrade(tradeId, buyerId);

		// then
		assertEquals(TradeStatus.PAYMENT_CANCELED, trade.getStatus());

		assertEquals(BidStatus.CANCELED, buyBid.getStatus(), "구매자 입찰은 취소되어야 한다");
		assertEquals(BidStatus.PENDING, sellBid.getStatus(), "판매자 입찰은 다시 대기 상태여야 한다");
		assertTrue(buyBid.getUser().isBidBlocked(), "취소한 사용자는 입찰 제한 상태여야 한다");

		verify(notificationService).createNotification(
			eq(buyerId),
			contains("입찰 등록이 제한")
		);

		verify(notificationService).createNotification(
			eq(sellerId),
			contains("다시 대기 상태")
		);
	}

	/**
	 * 체결된 거래를 판매자가 취소하는 경우를 테스트합니다.
	 * 1.Trade 상태가 PAYMENT_CANCELED로 변경된다
	 * 2.판매자의 입찰은 CANCELED 상태가 된다
	 * 3.구매자의 입찰은 다시 PENDING 상태로 변경된다
	 * 4.판매자에게 입찰 제한 패널티가 적용된다
	 * 5.구매자와 판매자에게 알림이 발송된다.
	 */
	@Test
	@DisplayName("체결된 거래를 판매자가 취소하면 상태가 정상적으로 변경된다")
	void cancelTradeBySellerSuccessTest() {
		// given
		Long tradeId = 11L;
		Long buyerId = 1L;
		Long sellerId = 2L;

		ProductOption option = createOption(1L);

		Bid buyBid = createMatchedBid(1L, buyerId, option, BidType.BUY);
		Bid sellBid = createMatchedBid(2L, sellerId, option, BidType.SELL);

		Trade trade = new Trade(buyBid, sellBid, 250000L);
		ReflectionTestUtils.setField(trade, "id", tradeId);

		when(tradeRepository.findById(tradeId))
			.thenReturn(Optional.of(trade));

		// when
		tradeService.cancelTrade(tradeId, sellerId);

		// then
		assertEquals(TradeStatus.PAYMENT_CANCELED, trade.getStatus());
		assertEquals(BidStatus.CANCELED, sellBid.getStatus());
		assertEquals(BidStatus.PENDING, buyBid.getStatus());
		assertTrue(sellBid.getUser().isBidBlocked());

		verify(notificationService, times(2))
			.createNotification(anyLong(), anyString());
	}

	/**
	 * 체결된 거래의 당사자가 아닌 사용자가 취소를 시도할 경우
	 * 예외가 발생하는지 검증합니다.
	 */
	@Test
	@DisplayName("체결 당사자가 아닌 사용자가 취소하면 예외가 발생한다")
	void cancelTradeByThirdUserFailTest() {
		// given
		Long tradeId = 12L;
		Long thirdUserId = 999L;

		ProductOption option = createOption(1L);

		Bid buyBid = createMatchedBid(1L, 1L, option, BidType.BUY);
		Bid sellBid = createMatchedBid(2L, 2L, option, BidType.SELL);

		Trade trade = new Trade(buyBid, sellBid, 250000L);
		ReflectionTestUtils.setField(trade, "id", tradeId);

		when(tradeRepository.findById(tradeId))
			.thenReturn(Optional.of(trade));

		// when & then
		assertThrows(
			BusinessException.class,
			() -> tradeService.cancelTrade(tradeId, thirdUserId)
		);
	}

	/**
	 * 이미 취소된 거래를 다시 취소하려는 경우
	 * 예외가 발생하는지 검증합니다.
	 */
	@Test
	@DisplayName("이미 취소된 거래를 다시 취소하면 예외가 발생한다")
	void cancelAlreadyCanceledTradeFailTest() {
		// given
		Trade trade = createWaitingPaymentTrade();
		trade.cancelPayment();

		given(tradeRepository.findById(1L))
			.willReturn(Optional.of(trade));

		// when & then
		assertThrows(
			BusinessException.class,
			() -> tradeService.cancelTrade(1L, 1L)
		);
	}

	/**
	 * 결제가 완료된 거래를 취소하려는 경우
	 * 예외가 발생하는지 검증합니다.
	 */
	@Test
	@DisplayName("결제가 완료된 거래는 취소할 수 없다")
	void cancelCompletedTradeFailTest() {
		// given
		Long tradeId = 14L;
		Long buyerId = 1L;

		ProductOption option = createOption(1L);

		Bid buyBid = createMatchedBid(1L, buyerId, option, BidType.BUY);
		Bid sellBid = createMatchedBid(2L, 2L, option, BidType.SELL);

		Trade trade = new Trade(buyBid, sellBid, 250000L);
		trade.completePayment();
		ReflectionTestUtils.setField(trade, "id", tradeId);

		when(tradeRepository.findById(tradeId))
			.thenReturn(Optional.of(trade));

		// when & then
		assertThrows(
			BusinessException.class,
			() -> tradeService.cancelTrade(tradeId, buyerId)
		);
	}

	/**
	 * 존재하지 않는 거래 ID로 취소를 시도할 경우
	 * 예외가 발생하는지 검증합니다.
	 */
	@Test
	@DisplayName("존재하지 않는 거래를 취소하면 예외가 발생한다")
	void cancelTradeNotFoundFailTest() {
		// given
		when(tradeRepository.findById(999L))
			.thenReturn(Optional.empty());

		// when & then
		assertThrows(
			BusinessException.class,
			() -> tradeService.cancelTrade(999L, 1L)
		);
	}

	/**
	 * MATCHED 상태의 입찰(Bid) 테스트 객체를 생성합니다.
	 *
	 * @param bidId 입찰 ID
	 * @param userId 사용자 ID
	 * @param option 상품 옵션
	 * @param type 입찰 타입(BUY / SELL)
	 * @return MATCHED 상태의 Bid 객체
	 */
	private Bid createMatchedBid(
		Long bidId,
		Long userId,
		ProductOption option,
		BidType type
	) {
		Users user = new Users(
			"test" + userId + "@test.com",
			"pw",
			"user" + userId,
			"010-1234-5678",
			UserRole.USER
		);
		ReflectionTestUtils.setField(user, "id", userId);

		Bid bid = Bid.builder()
			.user(user)
			.productOption(option)
			.price(250000L)
			.type(type)
			.status(BidStatus.MATCHED)
			.expiresAt(LocalDateTime.now().plusDays(7))
			.build();

		ReflectionTestUtils.setField(bid, "id", bidId);
		return bid;
	}

	/**
	 * 결제 대기 상태(WAITING_PAYMENT)의 Trade 테스트 객체를 생성합니다.
	 * 구매자와 판매자의 입찰은 모두 MATCHED 상태이며,
	 * Trade의 초기 상태는 WAITING_PAYMENT 입니다.
	 *
	 * @return WAITING_PAYMENT 상태의 Trade 객체
	 */
	private Trade createWaitingPaymentTrade() {
		ProductOption option = createOption(1L);

		Bid buyBid = createMatchedBid(1L, 1L, option, BidType.BUY);
		Bid sellBid = createMatchedBid(2L, 2L, option, BidType.SELL);

		return new Trade(buyBid, sellBid, 250_000L);
	}

}
