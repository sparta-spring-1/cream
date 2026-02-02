package com.sparta.cream.domain.trade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.cream.entity.UserRole;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.Users;

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

	/**
	 * 동일한 상품 옵션에 대해 구매가와 판매자가 일치할 경우 성공케이스
	 * 1. 입찰 상태가 MATCHED 로 변경되는지 확인
	 * 2. 새로운 Trade 엔티티가 생성되어 저장되는지 확인
	 */
	@Test
	@DisplayName("가격이 일치하는 구매와 판매 입찰이 있으면 체결이 성공한다")
	void matchBidsSuccessTest() {
		//given
		Long commonOptionId = 4L;
		ProductOption commonOption = createOption(commonOptionId);
		Bid buyBid = createBidWithOption(1L, commonOption, 1L, 250000L, BidType.BUY);
		Bid sellBid = createBidWithOption(2L, commonOption, 2L, 250000L, BidType.SELL);

		when(bidRepository.findByTypeAndStatusOrderByCreatedAtAsc(BidType.BUY, BidStatus.PENDING))
			.thenReturn(List.of(buyBid));

		when(bidRepository.findMatchingSellBids(
			eq(commonOptionId),
			anyLong(),
			anyLong(),
			any()
		)).thenReturn(List.of(sellBid));

		// when
		tradeService.matchAllPendingBids();

		// then
		assertEquals(BidStatus.MATCHED, buyBid.getStatus(), "구매 입찰이 MATCHED 상태여야 합니다.");
		assertEquals(BidStatus.MATCHED, sellBid.getStatus(), "판매 입찰이 MATCHED 상태여야 합니다.");
		verify(tradeRepository, times(1)).save(any(Trade.class));
	}


	/**
	 * 구매가가 판매가보다 낮을 경우 실패케이스 입니다
	 * 1. 입찰 상태가 여전히 PENDING 이어야 함
	 * 2. Trade 엔티티가 저장되지 않아야 함
	 */
	@Test
	@DisplayName("구매가가 판매가보다 낮으면 체결되지 않고 PENDING 상태를 유지한다")
	void matchBidsFailTest() {
		//given
		Long commonOptionId = 4L;
		ProductOption commonOption = createOption(commonOptionId);

		Bid buyBid = createBidWithOption(1L, commonOption, 1L, 200000L, BidType.BUY);
		Bid sellBid = createBidWithOption(2L, commonOption, 2L, 250000L, BidType.SELL);

		lenient().when(bidRepository.findAllByStatus(BidStatus.PENDING))
			.thenReturn(List.of(buyBid, sellBid));

		// when
		tradeService.matchAllPendingBids();

		// then
		assertEquals(BidStatus.PENDING, buyBid.getStatus());
		assertEquals(BidStatus.PENDING, sellBid.getStatus());
		verify(tradeRepository, never()).save(any(Trade.class));
	}

	/**
	 * 테스트용 상품 옵션 객체 생성 헬퍼 메서드
	 * Reflection 을 사용하여 protected 기본 생성자를 우회하고 ID를 강제 주입합니다.
	 * @param id 설정할 옵션 ID
	 * @return 생성된 ProductOption 객체
	 */
	private ProductOption createOption(Long id) {
		try {
			java.lang.reflect.Constructor<ProductOption> constructor = ProductOption.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			ProductOption option = constructor.newInstance();
			ReflectionTestUtils.setField(option, "id", id);
			return option;
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		Users user = new Users("test" + userId + "@test.com", "pw", "user" + userId, UserRole.USER);
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
}
