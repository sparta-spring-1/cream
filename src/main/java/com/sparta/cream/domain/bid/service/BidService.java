package com.sparta.cream.domain.bid.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.repository.ProductOptionRepository;

import lombok.RequiredArgsConstructor;

/**
 * 입찰(Bid) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * BidService.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */

@Service
@RequiredArgsConstructor
public class BidService {

	private final BidRepository bidRepository;
	private final ProductOptionRepository productOptionRepository;

	/**
	 * 새로운 입찰(구매 또는 판매)을 등록합니다.
	 * 1. 상품 옵션의 존재 여부를 확인합니다.
	 * 2. 가격이 0보다 큰지 검증합니다.
	 * 3. 입찰은 기본적으로 대기중(PENDING) 상태로 생성되며, 기본 만료일은 등록 시점으로부터 7일입니다.
	 * @param userId 입찰을 동록하는 사용자 식별자
	 * @param requestDto 입찰 요청 정보(상품 옵션 ID, 가격 타입등)
	 * @return 등록된 입찰정보(Response DTO)
	 */
	@Transactional
	public BidResponseDto createBid(Long userId, BidRequestDto requestDto) {
		ProductOption productOption = productOptionRepository.findById(requestDto.getProductOptionId())
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

		if (requestDto.getPrice() == null || requestDto.getPrice() <= 0) {
			throw new BusinessException(ErrorCode.INVALID_BID_PRICE);
		}

		Bid bid = Bid.builder()
			.userId(userId)
			.productOption(productOption)
			.price(requestDto.getPrice())
			.type(requestDto.getType())
			.status(BidStatus.PENDING)
			.expiresAt(LocalDateTime.now().plusDays(7))
			.build();

		Bid savedBid = bidRepository.save(bid);

		return new BidResponseDto(savedBid);
	}

	/**
	 * 특정 사용자의 입찰 내역을 최신순으로 조회합니다.
	 * @param userId 사용자 식별자
	 * @return 입찰 정보 응답 DTO 리스트
	 */
	@Transactional(readOnly = true)
	public List<BidResponseDto> getMyBids(Long userId) {

		List<Bid> bids = bidRepository.findAllByUserIdOrderByCreatedAtAsc(userId);

		return bids.stream()
			.map(BidResponseDto::new)
			.toList();
	}
}
