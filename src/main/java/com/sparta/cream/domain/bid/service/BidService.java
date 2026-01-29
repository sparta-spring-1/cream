package com.sparta.cream.domain.bid.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.domain.bid.dto.AdminBidCancelRequestDto;
import com.sparta.cream.domain.bid.dto.AdminBidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.BidCancelResponseDto;
import com.sparta.cream.domain.bid.entity.CancelReason;
import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BidErrorCode;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.repository.ProductOptionRepository;
import com.sparta.cream.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 입찰(Bid) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * BidService.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

	private final BidRepository bidRepository;
	private final ProductOptionRepository productOptionRepository;
	private final UserRepository userRepository;
	private final TradeService tradeService;

	/**
	 * 새로운 입찰(구매 또는 판매)을 등록하고, 즉시 매칭을 시도합니다.
	 * 1. 상품 옵션의 존재 여부를 확인합니다.
	 * 2. 기본 만료일은 등록 시점으로부터 7일로 설정됩니다.
	 * 3.등록 완료 후 {@link TradeService#processMatching(Bid)}을 호출하여 체결 가능 여부를 확인합니다.
	 * @param userId 입찰을 동록하는 사용자 식별자
	 * @param requestDto 입찰 요청 정보(상품 옵션 ID, 가격 타입등)
	 * @return 등록된 입찰정보(Response DTO)
	 */
	@Transactional
	public BidResponseDto createBid(Long userId, BidRequestDto requestDto) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		ProductOption productOption = productOptionRepository.findById(requestDto.getProductOptionId())
			.orElseThrow(() -> new BusinessException(BidErrorCode.PRODUCT_OPTION_NOT_FOUND));

		Bid bid = Bid.builder()
			.user(user)
			.productOption(productOption)
			.price(requestDto.getPrice())
			.type(requestDto.getType())
			.status(BidStatus.PENDING)
			.expiresAt(LocalDateTime.now().plusDays(7))
			.build();

		Bid savedBid = bidRepository.save(bid);

		tradeService.processMatching(savedBid);

		return new BidResponseDto(savedBid);
	}

	/**
	 * 특정 사용자의 입찰 내역을 최신순으로 조회합니다.
	 * @param userId 사용자 식별자
	 * @return 입찰 정보 응답 DTO 리스트
	 */
	@Transactional(readOnly = true)
	public Page<BidResponseDto> getMyBids(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Bid> bidPage = bidRepository. findAllByUserIdOrderByCreatedAtAsc(userId, pageable);

		return bidPage.map(BidResponseDto::new);
	}

	/**
	 * 특정 상품 옵션에 등록된 모든 입찰 내역을 조회합니다. (상품 상세 페이지용)
	 * @param productOptionId 상품 옵션 식별자
	 * @return 해당 상품의 입찰 정보 리스트
	 */
	@Transactional(readOnly = true)
	public List<BidResponseDto> getBidsByProductOption(Long productOptionId) {

		if (!productOptionRepository.existsById(productOptionId)) {
			throw new BusinessException(BidErrorCode.PRODUCT_OPTION_NOT_FOUND);
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
			.orElseThrow(() -> new BusinessException(BidErrorCode.BID_NOT_FOUND));

		if (!bid.getUserId().equals(userId)) {
			throw new BusinessException(BidErrorCode.NOT_YOUR_BID);
		}

		ProductOption newOption = productOptionRepository.findById(requestDto.getProductOptionId())
			.orElseThrow(() -> new BusinessException(BidErrorCode.PRODUCT_OPTION_NOT_FOUND));

		bid.update(requestDto.getPrice(), newOption, requestDto.getType());

		tradeService.processMatching(bid);

		return new BidResponseDto(bid);
	}

	/**
	 * 기존 입찰을 취소합니다.
	 * 입찰 취소에 대한 모든 검증 로직은 Bid 도메인 객체에 위임합니다.
	 * @param userId 입찰을 취소하려는 사용자 ID
	 * @param bidId 취소할 입찰 ID
	 * @return 입찰 취소 결과 응답 DTO
	 */
	@Transactional
	public BidCancelResponseDto cancelBid(Long userId, Long bidId) {
		Bid bid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(BidErrorCode.BID_NOT_FOUND));

		bid.cancel(userId);

		return BidCancelResponseDto.from(bid);
	}

	/**
	 * 관리자 권한으로 입찰을 강제 취소 처리합니다.
	 * 입력받은 사유 코드의 유효성 검증후, 해당 입찰의 상태를
	 * 관리자 취소로 변경하고 취소 이력(관리자ID, 사유, 상세 메모)를 저장합니다.
	 * @param bidId 취소 처리할 입찰의 고유 식별자
	 * @param request 취소사유 코드 및 상세 코멘트를 담은 요청 DTOO
	 * @return 취소된 입찰 정보와 처리 결과가 담긴 응답 DTO
	 */
	@Transactional
	public AdminBidCancelResponseDto cancelBidByAdmin(Long bidId, AdminBidCancelRequestDto request,Long adminId) {

		Users admin = userRepository.findById(adminId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		if (admin.getRole() != UserRole.ADMIN) {
			log.warn("권한 없는 사용자의 관리자 기능 접근 시도: userId={}", adminId);
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		try {
			CancelReason.valueOf(request.getReasonCode());
		} catch (IllegalArgumentException e) {
			throw new BusinessException(BidErrorCode.INVALID_REASON_CODE);
		}

		Bid bid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(BidErrorCode.BID_NOT_FOUND));

		log.info("관리자 취소 시작 - 입찰ID: {}, 관리자: {}", bidId, admin.getName());

		bid.cancelByAdmin(admin,request.getReasonCode(), request.getComment());

		return new AdminBidCancelResponseDto(
			bid.getId(),
			bid.getStatus(),
			admin.getName(),
			LocalDateTime.now().toString(),
			request.getReasonCode()
		);
	}

}
