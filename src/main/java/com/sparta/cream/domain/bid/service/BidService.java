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

	/**
	 * 특정 상품 옵션에 등록된 모든 입찰 내역을 조회합니다. (상품 상세 페이지용)
	 * @param productOptionId 상품 옵션 식별자
	 * @return 해당 상품의 입찰 정보 리스트
	 */
	@Transactional(readOnly = true)
	public List<BidResponseDto> getBidsByProductOption(Long productOptionId) {

		if (!productOptionRepository.existsById(productOptionId)) {
			throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
		}

		List<Bid> bids = bidRepository.findAllByProductOptionIdOrderByPriceDesc(productOptionId);

		return bids.stream()
			.map(BidResponseDto::new)
			.toList();
	}

	/**
	 * 기존 입찰 정보를 수정합니다
	 * 입찰 가격, 상품옵션, 입찰 타입(구매/판매)을 변경할수 있습니다.
	 * 대기(PENDING) 상태인 입찰만 수정이 가능합니다.
	 * 수정 권한은 해당 입찰을 등록한 본인에게만 잇습니다.
	 * @param userId 수정하는 사용자
	 * @param bidId 수정할 입찰 ID
	 * @param requestDto 수정할 새로운 정보 (상품 옵셔 ID, 가격, 타입)
	 * @return 수정이 완료된 입찰의 상세 정보
	 */
	@Transactional
	public BidResponseDto updateBid(Long userId, Long bidId, BidRequestDto requestDto) {
		Bid bid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(ErrorCode.BID_NOT_FOUND));

		ProductOption newOption = productOptionRepository.findById(requestDto.getProductOptionId())
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

		bid.update(requestDto.getPrice(), newOption, requestDto.getType());

		return new BidResponseDto(bid);
	}
}
