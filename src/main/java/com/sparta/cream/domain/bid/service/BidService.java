package com.sparta.cream.domain.bid.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.domain.bid.dto.AdminBidCancelRequestDto;
import com.sparta.cream.domain.bid.dto.AdminBidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.AdminBidMonitoringResponseDto;
import com.sparta.cream.domain.bid.dto.AdminBidPagingResponseDto;
import com.sparta.cream.domain.bid.dto.BidCancelResponseDto;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.entity.CancelReason;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.dto.AdminTradeMonitoringResponseDto;
import com.sparta.cream.domain.trade.dto.AdminTradePagingResponseDto;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.repository.TradeRepository;
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
	private final TradeRepository tradeRepository;
	private final NotificationService notificationService;

	/**
	 * 새로운 입찰(구매 또는 판매)을 등록하고, 즉시 매칭을 시도합니다.
	 * 1. 상품 옵션의 존재 여부를 확인합니다.
	 * 2. 기본 만료일은 등록 시점으로부터 7일로 설정됩니다.
	 * 3 입찰 등록 성공시, 해당 사용자에게 마이페이지 알림을 발송합니다.
	 * 4. 등록 완료후 {@link com.sparta.cream.domain.trade.service.TradeService#processMatching(Bid)}을
	 * 호출하여 즉시 체결 가능한 상대 입찰이 있는지 확인합니다.
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

		String message = String.format("[%s] %d원에 입찰이 등록되었습니다.",
			bid.getType().equals(BidType.BUY) ? "구매" : "판매",
			bid.getPrice());
		notificationService.createNotification(userId, message);

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

	/**
	 * 관리자 입찰 모니터링 목록을 조회합니다.
	 * 상품, 카테고리, 입찰 상태, 입찰 타입, 특정 유저 등의
	 * 선택적 필터 조건을 기반으로 입찰 데이터를 조회하며,
	 * 페이징 처리된 결과를 관리자용 DTO 형태로 반환합니다.
	 * 해당 메서드는 조회 전용 기능으로 {@code readOnly = true} 트랜잭션으로 실행됩니다.
	 *
	 * @param productId 조회할 상품 ID
	 * @param categoryId 조회할 카테고리 ID
	 * @param status 입찰 상태
	 * @param type 입찰 타입
	 * @param userId 특정 유저의 입찰 내역 조회 시 사용
	 * @param page 조회할 페이지 번호
	 * @return 관리자 입찰 모니터링 페이징 응답 DTO
	 */
	@Transactional(readOnly = true)
	public AdminBidPagingResponseDto getBidMonitoringList(Long productId,Long categoryId, String status, String type,Long userId, int page) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());

		Page<Bid> bidPage = bidRepository.findAllByMonitoringFilter(productId, categoryId, status, type, pageable, userId);

		List<AdminBidMonitoringResponseDto> items = bidPage.getContent().stream()
			.map(bid -> AdminBidMonitoringResponseDto.builder()
				.bidId(bid.getId())
				.userId(bid.getUser().getId())
				.userName(bid.getUser().getName())
				.productId(bid.getProductOption().getProduct().getId())
				.productName(bid.getProductOption().getProduct().getName())
				.categoryId(bid.getProductOption().getProduct().getProductCategory().getId())
				.categoryName(bid.getProductOption().getProduct().getProductCategory().getName())
				.price(bid.getPrice().longValue())
				.type(bid.getType().name())
				.status(bid.getStatus().name())
				.createdAt(bid.getCreatedAt())
				.build())
			.toList();

		AdminBidPagingResponseDto.PagingInfo pagingInfo = new AdminBidPagingResponseDto.PagingInfo(
			bidPage.getNumber(),
			bidPage.getTotalElements(),
			bidPage.hasNext()
		);

		return new AdminBidPagingResponseDto(items, pagingInfo);
	}

	/**
	 * 관리자 실시간 거래(Trade) 체결 모니터링 목록을 조회합니다.
	 * 거래 상태 및 특정 유저 조건을 기준으로
	 * 체결된 거래 내역을 조회하여 관리자용 DTO로 변환합니다.
	 * 판매/구매 입찰 정보가 누락된 경우를 고려하여
	 * 안전하게 값을 처리하도록 구성되어 있습니다.
	 *
	 * @param status 거래 상태
	 * @param userId 특정 유저의 거래 내역 조회 시 사용
	 * @param page 조회할 페이지 번호
	 * @return 관리자 거래 모니터링 페이징 응답 DTO
	 */
	@Transactional(readOnly = true)
	public AdminTradePagingResponseDto getTradeMonitoringList(String status, Long userId, Integer page) {
		Pageable pageable = PageRequest.of(page, 10);
		Page<Trade> tradePage = tradeRepository.findAllByTradeFilter(status, userId, pageable);

		List<AdminTradeMonitoringResponseDto> items = tradePage.getContent().stream()
			.map(t -> {
				Bid sBid = t.getSaleBidId();
				Bid pBid = t.getPurchaseBidId();

				return AdminTradeMonitoringResponseDto.builder()
					.tradeId(t.getId())
					.productName(sBid != null && sBid.getProductOption() != null
						? sBid.getProductOption().getProduct().getName() : "상품정보없음")
					.price(t.getFinalPrice())
					.status(t.getStatus() != null ? t.getStatus().name() : "상태없음")
					.sellerName(sBid != null && sBid.getUser() != null ? sBid.getUser().getName() : "판매자없음")
					.buyerName(pBid != null && pBid.getUser() != null ? pBid.getUser().getName() : "구매자없음")
					.createdAt(t.getCreatedAt())
					.build();
			})
			.toList();

		return new AdminTradePagingResponseDto(items,
			new AdminTradePagingResponseDto.PagingInfo(tradePage.getNumber(), tradePage.getTotalElements(), tradePage.hasNext())
		);
	}
}
