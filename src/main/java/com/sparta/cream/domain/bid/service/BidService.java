package com.sparta.cream.domain.bid.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sparta.cream.domain.bid.dto.AdminBidCancelRequestDto;
import com.sparta.cream.domain.bid.dto.AdminBidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.AdminBidMonitoringResponseDto;
import com.sparta.cream.domain.bid.dto.AdminBidPagingResponseDto;
import com.sparta.cream.domain.bid.dto.BidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.event.BidChangedEvent;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.entity.NotificationType;
import com.sparta.cream.domain.trade.dto.AdminTradeMonitoringResponseDto;
import com.sparta.cream.domain.trade.dto.AdminTradePagingResponseDto;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.repository.TradeRepository;
import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductOption;
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
	private final ApplicationEventPublisher eventPublisher;
	private final RedissonClient redissonClient;

	/**
	 * 새로운 입찰을 등록하고 매칭 엔진을 비동기적으로 가동합니다.
	 * 1. 페널티 검증: Redis 및 DB를 조회하여 현재 입찰 제한 상태인 사용자인지 확인합니다.
	 * 2. 데이터 영속화: 입찰 정보를 저장하고, 빠른 조회를 위해 Redis Sorted Set에 추가합니다.
	 * 3. 캐시 일관성: 해당 상품의 기존 입찰 목록 캐시({@code productBids})를 제거하여 데이터 최신성을 유지합니다.
	 * 4. 비동기 매칭 예약: DB 트랜잭션이 성공적으로 커밋된 직후({@code afterCommit}),
	 *  매칭 엔진({@link TradeService#handleMatchingInternal(Long)})을 호출하여 체결 프로세스를 시작합니다.
	 *
	 * @param userId 입찰을 등록하는 사용자의 고유 식별자
	 * @param requestDto 입찰 가격, 타입(구매/판매), 상품 옵션 정보를 담은 DTO
	 * @return 등록된 입찰 정보가 담긴 {@link BidResponseDto}
	 */
	@Transactional
	@CacheEvict(value = "productBids", key = "#requestDto.productOptionId")
	public BidResponseDto createBid(Long userId, BidRequestDto requestDto) {
		validateUserPenalty(userId);

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
		addToRedisZSet(savedBid);

		eventPublisher.publishEvent(new BidChangedEvent(
			userId,
			NotificationType.BID_REGISTERED,
			"입찰 등록 완료",
			String.format("[%s] %d원에 입찰 등록 완료", bid.getType(), bid.getPrice()),
			null
		));
		registerMatchingSync(savedBid.getId());

		return new BidResponseDto(savedBid);
	}

	/**
	 * 특정 사용자가 등록한 모든 입찰 내역을 페이지 단위로 조회합니다.
	 * 1.페이징 적용:요청된 페이지 번호와 사이즈에 맞춰 필요한 만큼의 데이터만 효율적으로 가져옵니다.
	 * 2.정렬 기준: 최신 등록순({@code CreatedAtAsc})으로 정렬하여 사용자에게 최근 활동을 먼저 보여줍니다.
	 * 3.성능 최적화:{@code readOnly = true} 설정을 통해 조회 시 발생하는 하이버네이트 스냅샷 생성 및 변경 감지(Dirty Check) 오버헤드를 방지합니다
	 * 4.DTO 변환:엔티티 모델이 외부에 노출되지 않도록 {@link BidResponseDto}로 변환하여 반환합니다.
	 *
	 * @param userId 조회를 요청한 사용자의 ID
	 * @param page 조회할 페이지 번호
	 * @param size 한 페이지에 표시할 입찰 내역 개수
	 * @return 페이징 정보가 포함된 입찰 응답 DTO 페이지 객체
	 */
	@Transactional(readOnly = true)
	public Page<BidResponseDto> getMyBids(Long userId, Integer page, Integer size) {
		int validatedPage = (page == null || page < 0) ? 0 : page;
		int validatedSize = (size == null || size < 1) ? 10 : size;

		Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by("createdAt").ascending());
		Page<Bid> bidPage = bidRepository. findAllByUserId(userId, pageable);

		return bidPage.map(BidResponseDto::new);
	}

	/**
	 * 특정 상품 옵션에 등록된 모든 입찰 내역을 가격 순으로 조회합니다.
	 * 1. 유효성 검증: 조회를 시작하기 전 해당 상품 옵션({@code productOptionId})이 존재하는지 먼저 확인합니다.
	 * 2.정렬 기준: 가격 내림차순({@code PriceDesc})으로 정렬하여, 구매자에게는 최적의 판매가를,
	 * 판매자에게는 최고 구매가를 상단에 노출하기 용이하게 제공합니다.
	 * 3.성능 최적화: {@code readOnly = true} 설정을 통해 조회 전용 트랜잭션으로 처리하여 성능 효율을 높였습니다.
	 *
	 * @param productOptionId 입찰 내역을 확인할 상품 옵션 식별자
	 * @return 해당 상품 옵션의 입찰 정보 응답 DTO 리스트
	 * @throws BusinessException 존재하지 않는 상품 옵션일 경우 발생
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
	 * 기존에 등록된 입찰 정보를 수정하고, 변경된 조건으로 매칭을 다시 시도합니다.
	 * 1. 권한 및 페널티 검증: 사용자의 입찰 제한 여부와 해당 입찰의 소유권을 확인합니다.
	 * 2. Redis 동기화 (Remove): 정보가 수정되기 전, 기존 조건으로 정렬되어 있던 Redis ZSet 데이터를 먼저 제거합니다.
	 * 3. 데이터 업데이트: 입찰 가격, 상품 옵션, 타입 등의 정보를 새롭게 갱신합니다.
	 * 4. Redis 동기화 (Add): 변경된 가격과 조건에 맞춰 Redis ZSet에 새로운 스코어로 데이터를 추가합니다.
	 * 5. 비동기 매칭 재가동: 수정 사항이 DB에 최종 커밋된 후, 변경된 조건으로 즉시 체결이 가능한지 매칭 엔진을 호출합니다.
	 *
	 * @param userId 수정을 요청한 사용자의 ID
	 * @param bidId 수정할 입찰의 고유 식별자
	 * @param requestDto 변경할 가격, 상품 옵션, 타입 정보를 담은 DTO
	 * @return 수정이 완료된 입찰 정보 응답 DTO
	 * @throws BusinessException 페널티 유저, 본인 입찰이 아님, 또는 입찰/상품 정보가 없을 경우 발생
	 */
	@Transactional
	@CacheEvict(value = "productBids", key = "#requestDto.productOptionId")
	public BidResponseDto updateBid(Long userId, Long bidId, BidRequestDto requestDto) {
		validateUserPenalty(userId);

		Bid bid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(BidErrorCode.BID_NOT_FOUND));

		if (!bid.getUser().getId().equals(userId)) {
			throw new BusinessException(BidErrorCode.NOT_YOUR_BID);
		}


		Long oldPrice = bid.getPrice();
		BidType oldType = bid.getType();

		removeFromRedisZSet(bid);

		ProductOption newOption = productOptionRepository.findById(requestDto.getProductOptionId())
			.orElseThrow(() -> new BusinessException(BidErrorCode.PRODUCT_OPTION_NOT_FOUND));

		bid.update(requestDto.getPrice(), newOption, requestDto.getType());

		addToRedisZSet(bid);

		registerMatchingSync(bid.getId());

		eventPublisher.publishEvent(new BidChangedEvent(
			userId,
			NotificationType.BID_UPDATED,
			"입찰 수정 완료",
			String.format(
				"입찰이 수정되었습니다.\n옵션: %s\n가격: %,d원 → %,d원\n유형: %s → %s",
				newOption.getSize(),
				oldPrice,
				bid.getPrice(),
				oldType,
				bid.getType()
			),
			bid.getId()
		));

		return new BidResponseDto(bid);
	}

	/**
	 * 사용자가 등록한 입찰을 안전하게 취소 처리합니다.
	 * 1.상태 사전 검증: 취소 요청을 받은 입찰이 현재 '대기(PENDING)' 상태인지 1차로 확인합니다.
	 * 2.분산 락 획득: 취소 처리 도중 해당 입찰이 매칭 엔진에 의해 체결되는 것을 방지하기 위해 상품 옵션 단위의 분산 락을 획득합니다.
	 * 3 데이터 재검증 (Double-Check):</b> 락을 획득한 직후, 그사이 체결 프로세스가 완료되었을 가능성을 배제하기 위해 입찰의 최신 상태를 DB에서 다시 조회하여 검증합니다.
	 * 4.상태 변경 및 정리: 입찰 상태를 '취소'로 변경하고, Redis 대기열(ZSet)에서 해당 데이터를 즉시 제거하여 이후 매칭 대상에서 제외합니다.
	 *
	 * @param userId 취소를 요청한 사용자의 ID
	 * @param bidId 취소할 입찰의 고유 식별자
	 * @return 취소 처리된 입찰의 정보를 담은 {@link BidCancelResponseDto}
	 */
	@Transactional
	@CacheEvict(value = "productBids", key = "#result.productOptionId")
	public BidCancelResponseDto cancelBid(Long userId, Long bidId) {
		Bid bid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(BidErrorCode.BID_NOT_FOUND));

		if (!bid.getUser().getId().equals(userId)) {
			throw new BusinessException(BidErrorCode.NOT_YOUR_BID);
		}

		if (bid.getStatus() != BidStatus.PENDING) {
			throw new BusinessException(BidErrorCode.CANNOT_CANCEL_NON_PENDING_BID);
		}

		bid.cancel(userId);
		removeFromRedisZSet(bid);

		eventPublisher.publishEvent(new BidChangedEvent(
			userId,
			NotificationType.BID_CANCELLED,
			"입찰 취소 완료",
			String.format(
				"[%s] 입찰 취소가 정상적으로 처리되었습니다.\n상품명: %s\n사이즈: %s",
				bid.getType(),
				bid.getProductOption().getProduct().getName(),
				bid.getProductOption().getSize()
			),
			bid.getId()
		));

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

		if (admin.getRole() != UserRole.ADMIN)
			throw new BusinessException(ErrorCode.ACCESS_DENIED);

		Bid bid = bidRepository.findById(bidId)
			.orElseThrow(() -> new BusinessException(BidErrorCode.BID_NOT_FOUND));

		if (bid.getStatus() != BidStatus.PENDING) {
			throw new BusinessException(BidErrorCode.CANNOT_CANCEL_NON_PENDING_BID);
		}

		bid.cancelByAdmin(admin,request.getReasonCode(), request.getComment());

		removeFromRedisZSet(bid);

		String cacheKey = "productBids::" + bid.getProductOption().getId();
		redissonClient.getBucket(cacheKey).delete();

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
	public AdminBidPagingResponseDto getBidMonitoringList(Long productId,Long categoryId, String status, String type,Long userId, Integer page) {
		int validatedPage = (page == null || page < 0) ? 0 : page;

		Pageable pageable = PageRequest.of(validatedPage, 10, Sort.by("createdAt").descending());

		Page<Bid> bidPage = bidRepository.findAllByMonitoringFilter(productId, categoryId, status, type, pageable, userId);

		List<AdminBidMonitoringResponseDto> items = bidPage.getContent().stream()
			.map(bid -> {
					Users user = bid.getUser();
					Product product = bid.getProductOption().getProduct();

					return AdminBidMonitoringResponseDto.builder()
					.bidId(bid.getId())
					.userId(user.getId())
					.userName(user.getName())
					.productName(product.getName())
					.price(bid.getPrice().longValue())
					.type(bid.getType().name())
					.status(bid.getStatus().name())
					.createdAt(bid.getCreatedAt())
					.build();
			})
			.toList();


		return new AdminBidPagingResponseDto(items,
			new AdminBidPagingResponseDto.PagingInfo(
				bidPage.getNumber(),
				bidPage.getTotalElements(),
				bidPage.hasNext()));
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
		int validatedPage = (page == null || page < 0) ? 0 : page;

		Pageable pageable = PageRequest.of(validatedPage, 10, Sort.by("createdAt").descending());

		Page<Trade> tradePage = tradeRepository.findAllByTradeFilter(status, userId, pageable);

		List<AdminTradeMonitoringResponseDto> items = tradePage.getContent().stream()
			.map(t -> {
				Bid saleBid = t.getSaleBidId();
				Bid purchaseBid = t.getPurchaseBidId();

			return AdminTradeMonitoringResponseDto.builder()
				.tradeId(t.getId())
				.productName(saleBid.getProductOption().getProduct().getName())
				.price(t.getFinalPrice())
				.status(t.getStatus().name())
				.sellerName(saleBid.getUser().getName())
				.buyerName(purchaseBid.getUser().getName())
				.createdAt(t.getCreatedAt())
				.build();
		})
			.toList();

		return new AdminTradePagingResponseDto(items,
			new AdminTradePagingResponseDto.PagingInfo(
				tradePage.getNumber(),
				tradePage.getTotalElements(),
				tradePage.hasNext()
			)
		);
	}

	/**
	 * 사용자 입찰 제한 상태를 검증합니다
	 * 1.Redis 캐시를 조회하여 현재 패널티 기간인지 확인합니다.
	 * 2. 캐시에 정보가 없는 경우 DB의 유저 상태를 확인하며, 차단 상태일 경우 Redis에 패널티 정보를 동기화합니다.
	 * @param userId 검증할 사용자의 고유 식별자
	 */
	private void validateUserPenalty(Long userId) {
		String penaltyKey = "user:penalty:" + userId;
		if (redissonClient.getBucket(penaltyKey).isExists()) {
			throw new BusinessException(BidErrorCode.BID_BLOCKED_BY_PENALTY);
		}

		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		if (user.isBidBlocked()) {
			redissonClient.getBucket(penaltyKey).set("BANNED", 3, TimeUnit.DAYS);
			throw new BusinessException(BidErrorCode.BID_BLOCKED_BY_PENALTY);
		}
	}

	/**
	 * DB 트랜잭션이 최종 커밋된 실시간 매칭 엔진을 호출하도록 예약합니다
	 * {@link TransactionSynchronizationManager}를 사용하여 현재 트랜잭션이 성공적으로 커밋된 시점({@code afterCommit})에
	 * {@link TradeService#handleMatchingInternal(Long)}을 비동기적으로 가동합니다
	 * 이를 통해 데이터가 DB에 완전히 반영되지 않은 상태에서 매칭이 시도되는 정합성 문제를 방지합니다.
	 *
	 * @param bidId 매칭 엔진에 전달할 입찰의 고유 식별자
	 */
	private void registerMatchingSync(final Long bidId) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				tradeService.handleMatchingInternal(bidId);
			}
		});
	}

	/**
	 * 입찰 정보를 Redis Sorted Set(ZSet)에 추가하여 실시간 매칭 대기열에 등록합니다.
	 * 1. 가격운선: 구매입찰은 높은 가격순, 판매 입찰은 낮은 가격순 정렬됩니다.
	 * 2.시간 우선: 가격이 동일한 경우, 먼저 등록된 입찰이 우선권을 갖도록 등록 시간 가중치를 더합니다.
	 *
	 * @param bid Redis 대기열에 추가할 입찰 객체
	 */
	private void addToRedisZSet(Bid bid) {
		String key = (bid.getType() == BidType.BUY ? "bids:buy:" : "bids:sell:") + bid.getProductOption().getId();
		RScoredSortedSet<Long> zset = redissonClient.getScoredSortedSet(key);

		double timeWeight = System.currentTimeMillis() / 10000000000000.0;
		double score;
		if (bid.getType() == BidType.BUY) {
			score = -bid.getPrice().doubleValue() + timeWeight;
		} else {
			score = bid.getPrice().doubleValue() + timeWeight;
		}

		zset.add(score, bid.getId());
	}

	/**
	 * Redis Sorted Set 대기열에서 특정 입찰을 제거합니다.
	 * 체결완료, 정보 수정 전 기존 데이터 기록 삭제, 또는 취소시 호출됩니다.
	 * @param bid 제거할 입찰 객체
	 */
	private void removeFromRedisZSet(Bid bid) {
		String key = (bid.getType() == BidType.BUY ? "bids:buy:" : "bids:sell:") + bid.getProductOption().getId();
		redissonClient.getScoredSortedSet(key).remove(bid.getId());
	}

}
