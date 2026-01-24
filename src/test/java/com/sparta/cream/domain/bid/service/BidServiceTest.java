package com.sparta.cream.domain.bid.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.repository.ProductOptionRepository;

/**
 * 입찰 서비스(BidService) 단위테스트입니다.
 * Mockito를 사용하여 리포지토리 의존성을 분리하고, 입찰 기능의 성공 및 실패 시나리오 검증합니다.
 * BidServiceTest.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@ExtendWith(MockitoExtension.class)
class BidServiceTest {

	@InjectMocks
	private BidService bidService;

	@Mock
	private BidRepository bidRepository;

	@Mock
	private ProductOptionRepository productOptionRepository;

	/**
	 * 입찰 등록 성공 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("입찰 등록 성공 테스트")
	void createBid_Success() {
		// given
		Long userId = 1L;
		Long productOptionId = 100L;
		Long price = 150000L;

		BidRequestDto requestDto = new BidRequestDto(productOptionId, price, BidType.BUY);
		ProductOption productOption = mock(ProductOption.class);

		given(productOptionRepository.findById(productOptionId)).willReturn(Optional.of(productOption));

		// save
		given(bidRepository.save(any(Bid.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		BidResponseDto response = bidService.createBid(userId, requestDto);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getPrice()).isEqualTo(price);
		assertThat(response.getStatus()).isEqualTo(BidStatus.PENDING);
		assertThat(response.getType()).isEqualTo(BidType.BUY);

		verify(bidRepository, times(1)).save(any(Bid.class));
	}

	/**
	 * 존재하지 않는 상품 옵션에 대한 입찰 등록 실패 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("입찰 등록 실패 - 상품 옵션이 없는 경우")
	void createBid_Fail_ProductOptionNotFound() {
		// given
		Long userId = 1L;
		BidRequestDto requestDto = new BidRequestDto(999L, 10000L, BidType.BUY);

		given(productOptionRepository.findById(anyLong())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> bidService.createBid(userId, requestDto))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.PRODUCT_OPTION_NOT_FOUND.getMessage());
	}

	/**
	 * 유요하지 않는 입찰 가겨 (0원이하)에 대한 등록 실패 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("입찰 등록 실패 - 가격이 유효하지 않은 경우 (0원 이하)")
	void createBid_Fail_InvalidPrice() {
		// given
		Long userId = 1L;
		Long productOptionId = 100L;
		BidRequestDto requestDto = new BidRequestDto(productOptionId, 0L, BidType.BUY);

		ProductOption productOption = mock(ProductOption.class);
		given(productOptionRepository.findById(productOptionId)).willReturn(Optional.of(productOption));

		// when & then
		assertThatThrownBy(() -> bidService.createBid(userId, requestDto))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_BID_PRICE.getMessage());
	}

	/**
	 * 내 입찰 내역 조회 성공 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("내 입찰 내역 조회 성공 - 목록 반환 확인")
	void getMyBids_Success() {
		// given
		Long userId = 1L;
		ProductOption productOption = mock(ProductOption.class);

		Bid bid1 = Bid.builder()
			.userId(userId)
			.productOption(productOption)
			.price(100000L)
			.type(BidType.BUY)
			.status(BidStatus.PENDING)
			.build();

		Bid bid2 = Bid.builder()
			.userId(userId)
			.productOption(productOption)
			.price(120000L)
			.type(BidType.BUY)
			.status(BidStatus.PENDING)
			.build();

		given(bidRepository.findAllByUserIdOrderByCreatedAtAsc(userId))
			.willReturn(List.of(bid1, bid2));

		// when
		List<BidResponseDto> response = bidService.getMyBids(userId);

		// then
		assertThat(response).hasSize(2);
		assertThat(response.get(0).getPrice()).isEqualTo(100000L);
		assertThat(response.get(1).getPrice()).isEqualTo(120000L);

		verify(bidRepository, times(1)).findAllByUserIdOrderByCreatedAtAsc(userId);
	}

	/**
	 * 입찰 내역이 없을 때 빈 리스트가 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("내 입찰 내역 조회 성공 - 내역이 없는 경우 빈 리스트 반환")
	void getMyBids_Success_EmptyList() {
		// given
		Long userId = 1L;
		given(bidRepository.findAllByUserIdOrderByCreatedAtAsc(userId))
			.willReturn(Collections.emptyList());

		// when
		List<BidResponseDto> response = bidService.getMyBids(userId);

		// then
		assertThat(response).isEmpty();
		assertThat(response).isNotNull();
	}
}

