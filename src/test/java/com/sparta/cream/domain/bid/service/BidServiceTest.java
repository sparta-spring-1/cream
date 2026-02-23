package com.sparta.cream.domain.bid.service;

import static com.sparta.cream.domain.bid.entity.QBid.*;
import static com.sparta.cream.entity.QProductOption.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sparta.cream.domain.bid.dto.AdminBidCancelRequestDto;
import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import com.sparta.cream.domain.bid.entity.CancelReason;
import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BidErrorCode;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.repository.ProductOptionRepository;
import com.sparta.cream.repository.UserRepository;

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

	@Mock
	private NotificationService notificationService;

	@Mock
	private TradeService tradeService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RBucket<Object> rBucket;

	@Mock
	private RScoredSortedSet<Object> rScoredSortedSet;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private Users testUser;
	private final Long userId = 1L;

	@BeforeEach
	void setUp() {
		testUser = mock(Users.class);
		lenient().when(testUser.getId()).thenReturn(userId);
		lenient().when(redissonClient.getBucket(anyString())).thenReturn(rBucket);
		lenient().when(rBucket.isExists()).thenReturn(false); // 기본적으로 패널티 없는 상태
		lenient().when(redissonClient.getScoredSortedSet(anyString())).thenReturn(rScoredSortedSet);
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void tearDown() {
		TransactionSynchronizationManager.clearSynchronization();
	}


	/**
	 * 입찰 등록 성공 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("입찰 등록 성공 테스트")
	void createBid_Success() {
		// given
		Long productOptionId = 100L;
		Long price = 150000L;
		Long fakeBidId = 1L;

		BidRequestDto requestDto = new BidRequestDto(productOptionId, price, BidType.BUY);
		ProductOption productOption = mock(ProductOption.class);

		given(productOption.getId()).willReturn(productOptionId);
		given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
		given(testUser.isBidBlocked()).willReturn(false);
		given(productOptionRepository.findById(productOptionId)).willReturn(Optional.of(productOption));
		given(bidRepository.save(any(Bid.class))).willAnswer(invocation -> {
				Bid bid = invocation.getArgument(0);
				org.springframework.test.util.ReflectionTestUtils.setField(bid, "id", fakeBidId);
				return bid;
			});

		// when
		BidResponseDto response = bidService.createBid(userId, requestDto);

		// then
		assertThat(response).isNotNull();
		verify(rScoredSortedSet, times(1)).add(anyDouble(), anyLong());
		verify(bidRepository, times(1)).save(any(Bid.class));
	}

	/**
	 * 존재하지 않는 상품 옵션에 대한 입찰 등록 실패 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("입찰 등록 실패 - 상품 옵션이 없는 경우")
	void createBid_Fail_ProductOptionNotFound() {
		// given
		BidRequestDto requestDto = new BidRequestDto(999L, 10000L, BidType.BUY);

		given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
		given(productOptionRepository.findById(anyLong())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> bidService.createBid(userId, requestDto))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(BidErrorCode.PRODUCT_OPTION_NOT_FOUND.getMessage());
	}


	/**
	 * 내 입찰 내역 조회 성공 시나리오를 검증합니다.
	 */
	@Test
	@DisplayName("내 입찰 내역 조회 성공 - 페이징 적용, 목록 반환 확인")
	void getMyBids_Success_WithPaging() {
		// given
		ProductOption productOption = mock(ProductOption.class);

		Bid bid1 = Bid.builder()
			.user(testUser)
			.productOption(productOption)
			.price(100000L)
			.type(BidType.BUY)
			.status(BidStatus.PENDING)
			.build();

		Bid bid2 = Bid.builder()
			.user(testUser)
			.productOption(productOption)
			.price(120000L)
			.type(BidType.BUY)
			.status(BidStatus.PENDING)
			.build();

		int page = 0;
		int size = 10;
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

		Page<Bid> bidPage = new PageImpl<>(List.of(bid1, bid2), pageable, 2);

		given(bidRepository.findAllByUserId(userId, pageable))
			.willReturn(bidPage);

		// when
		Page<BidResponseDto> response = bidService.getMyBids(userId, page, size);

		// then
		assertThat(response.getContent()).hasSize(2);
		assertThat(response.getContent().get(0).getPrice()).isEqualTo(100000L);
		assertThat(response.getContent().get(1).getPrice()).isEqualTo(120000L);

		verify(bidRepository, times(1)).findAllByUserId(userId, pageable);
	}

	/**
	 * 내 입찰 내역 조회 성공 - 내역이 없는 경우 빈 페이지 반환
	 */
	@Test
	@DisplayName("내 입찰 내역 조회 성공 - 페이징 적용, 내역이 없는 경우")
	void getMyBids_Success_EmptyPage() {
		// given
		Long userId = 1L;
		int page = 0;
		int size = 10;
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

		Page<Bid> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

		given(bidRepository.findAllByUserId(userId, pageable))
			.willReturn(emptyPage);

		// when
		Page<BidResponseDto> response = bidService.getMyBids(userId, page, size);

		// then
		assertThat(response.getContent()).isEmpty();
		assertThat(response.getTotalElements()).isEqualTo(0);

		verify(bidRepository, times(1)).findAllByUserId(userId, pageable);
	}

	/**
	 * 상품별 입찰 조회 성공 시나리오
	 */
	@Test
	@DisplayName("상품별 입찰 조회 성공 - 가격 내림차순 확인")
	void getBidsByProductOption_Success() {
		// given
		Long productOptionId = 1L;
		ProductOption productOption = mock(ProductOption.class);
		ReflectionTestUtils.setField(productOption, "id", productOptionId);

		Bid bid1 = Bid.builder().price(10000L).productOption(productOption).build();
		Bid bid2 = Bid.builder().price(20000L).productOption(productOption).build();

		given(productOptionRepository.existsById(productOptionId)).willReturn(true);
		given(bidRepository.findAllByProductOptionIdOrderByPriceDesc(productOptionId)).willReturn(List.of(bid2, bid1));

		// when
		List<BidResponseDto> result = bidService.getBidsByProductOption(productOptionId);

		// then
		assertThat(result.get(0).getPrice()).isEqualTo(20000L);
		assertThat(result.get(1).getPrice()).isEqualTo(10000L);
	}

	/**
	 * 존재하지 않는 상품 옵션 조회시 실패 시나리오
	 */
	@Test
	@DisplayName("상품별 입찰 조회 실패 - 존재하지 않는 상품 옵션")
	void getBidsByProductOption_Fail_NotFound() {
		// given
		Long productOptionId = 999L;
		given(productOptionRepository.existsById(productOptionId)).willReturn(false);

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> bidService.getBidsByProductOption(productOptionId));

		assertThat(exception.getErrorCode()).isEqualTo(BidErrorCode.PRODUCT_OPTION_NOT_FOUND);
	}

	/**
	 * 입찰 수정 성공 시나리오를 검증합니다. (가격, 옵션, 타입 모두 변경)
	 */
	@Test
	@DisplayName("입찰 수정 성공 - 가격, 옵션, 타입이 정상적으로 변경되어야 함")
	void updateBid_Success() {
		// given
		Long bidId = 100L;
		Long oldProductOptionId = 50L;
		Long newPrice = 200000L;
		Long newProductOptionId = 101L;
		BidType newType = BidType.SELL;

		BidRequestDto requestDto = new BidRequestDto(newProductOptionId, newPrice, newType);

		ProductOption oldOption = mock(ProductOption.class);
		ProductOption newOption = mock(ProductOption.class);
		lenient().when(oldOption.getId()).thenReturn(oldProductOptionId);
		lenient().when(newOption.getId()).thenReturn(newProductOptionId);

		Bid bid = Bid.builder()
			.user(testUser)
			.productOption(oldOption)
			.status(BidStatus.PENDING)
			.price(150000L)
			.type(BidType.BUY)
			.build();
		ReflectionTestUtils.setField(bid, "id", bidId);

		given(bidRepository.findById(bidId)).willReturn(Optional.of(bid));
		given(productOptionRepository.findById(newProductOptionId)).willReturn(Optional.of(newOption));
		lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
		lenient().when(testUser.isBidBlocked()).thenReturn(false);

		// when
		BidResponseDto response = bidService.updateBid(userId, bidId, requestDto);

		// then
		assertThat(response.getPrice()).isEqualTo(200000L);
		assertThat(response.getType()).isEqualTo(newType);
		verify(bidRepository).findById(bidId);
		verify(rScoredSortedSet, atLeastOnce()).add(anyDouble(), eq(bidId));
	}

	/**
	 * 이미 MATCHED 상태인 입찰 수정 실패 시나리오
	 */
	@Test
	@DisplayName("입찰 수정 실패 - 이미 체결(MATCHED)된 경우")
	void updateBid_Fail_AlreadyMatched() {
		// given
		Long userId = 1L;
		Long bidId = 100L;
		BidRequestDto requestDto = new BidRequestDto(101L, 200000L, BidType.SELL);


		ProductOption productOption = mock(ProductOption.class);
		lenient().when(productOption.getId()).thenReturn(1L);

		Bid bid = Bid.builder()
			.user(testUser)
			.productOption(productOption)
			.status(BidStatus.MATCHED)
			.price(150000L)
			.type(BidType.BUY)
			.build();
		ReflectionTestUtils.setField(bid, "id", bidId);

		given(bidRepository.findById(bidId)).willReturn(Optional.of(bid));
		lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
		lenient().when(testUser.isBidBlocked()).thenReturn(false);
		lenient().when(productOptionRepository.findById(anyLong())).thenReturn(Optional.of(mock(ProductOption.class)));

		// when & then
		assertThatThrownBy(() -> bidService.updateBid(userId, bidId, requestDto))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(BidErrorCode.CANNOT_UPDATE_BID.getMessage());
	}

	/**
	 * 본인의 입찰이 아닌경우 수정시 실패 시나리오
	 */
	@Test
	@DisplayName("입찰 수정 실패 - 본인 입찰이 아닌 경우")
	void updateBid_Fail_NotYourBid() {
		// given
		Long bidId = 100L;
		Long otherUserId = 999L;

		ProductOption productOption = mock(ProductOption.class);
		lenient().when(productOption.getId()).thenReturn(1L);


		Users otherUser = mock(Users.class);
		given(otherUser.getId()).willReturn(otherUserId);

		Bid bid = Bid.builder()
			.user(otherUser)
			.productOption(productOption)
			.status(BidStatus.PENDING)
			.build();
		ReflectionTestUtils.setField(bid, "id", bidId);

		given(bidRepository.findById(bidId)).willReturn(Optional.of(bid));

		lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
		lenient().when(testUser.isBidBlocked()).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> bidService.updateBid(userId, bidId, new BidRequestDto(101L, 200000L, BidType.SELL)))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(BidErrorCode.NOT_YOUR_BID.getMessage());
	}

	/**
	 * 패널티로 인한 유저가 입찰 수정시 싶패 테스트
	 */
	@Test
	@DisplayName("입찰 수정 실패 - 패널티로 인해 입찰이 차단된 유저")
	void updateBid_Fail_PenaltyUser() {
		// given
		Long testBidId = 1L;
		Long newOptionId = 100L;
		BidRequestDto requestDto = new BidRequestDto(newOptionId, 200000L, BidType.SELL);

		RBucket<Object> mockBucket = mock(RBucket.class);
		given(redissonClient.getBucket(anyString())).willReturn(mockBucket);
		given(mockBucket.isExists()).willReturn(false);

		ReflectionTestUtils.setField(testUser, "id", userId);
		given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
		given(testUser.isBidBlocked()).willReturn(true);

		// ReflectionTestUtils.setField(testUser, "id", userId);
		// given(testUser.getId()).willReturn(userId);
		// given(testUser.isBidBlocked()).willReturn(true);
		//
		// Bid bid = Bid.builder()
		// 	.user(testUser)
		// 	.productOption(oldOption)
		// 	.status(BidStatus.PENDING)
		// 	.price(150000L)
		// 	.type(BidType.BUY)
		// 	.build();
		// ReflectionTestUtils.setField(bid, "id", testBidId);
		//
		// given(bidRepository.findById(testBidId)).willReturn(Optional.of(bid));
		// given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

		// when & then
		assertThatThrownBy(() ->
			bidService.updateBid(userId, testBidId, new BidRequestDto(newOptionId, 200000L, BidType.SELL))
		)
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(BidErrorCode.BID_BLOCKED_BY_PENALTY.getMessage());
		verify(mockBucket).set(eq("BANNED"), eq(3L), eq(TimeUnit.DAYS));

	}

	/**
	 * 본인의 입찰이 아닌경우 취소시 실패 시나리오
	 */
	@Test
	@DisplayName("입찰 취소 실패 - 본인 입찰이 아닌 경우")
	void cancelBid_Fail_NotYourBid() {
		// given
		Long userId = 1L;
		Long bidId = 100L;
		Long otherUserId = 999L;

		Users otherUser = mock(Users.class);
		given(otherUser.getId()).willReturn(999L);

		Bid bid = Bid.builder()
			.user(otherUser)
			.status(BidStatus.PENDING)
			.build();

		given(bidRepository.findById(bidId)).willReturn(Optional.of(bid));

		// when & then
		assertThatThrownBy(() -> bidService.cancelBid(userId, bidId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(BidErrorCode.NOT_YOUR_BID.getMessage());
	}

	/**
	 * 관리자 권한으로 입찰 취소시 성공 시나리오
	 */
	@Test
	@DisplayName("관리자 권한으로 입찰 취소 성공")
	void cancelBidByAdmin_Success() {
		// given
		Long bidId = 1L;
		Long adminId = 99L;

		RBucket<Object> mockBucket = mock(RBucket.class);
		RScoredSortedSet mockZSet = mock(RScoredSortedSet.class);

		given(redissonClient.getBucket(anyString())).willReturn(mockBucket);
		given(redissonClient.getScoredSortedSet(anyString())).willReturn(mockZSet);

		ProductOption productOption = mock(ProductOption.class);
		given(productOption.getId()).willReturn(10L);

		AdminBidCancelRequestDto requestDto = new AdminBidCancelRequestDto(
			CancelReason.MISTAKE.name(),
			"관리자 수동 취소 처리"
		);

		Users adminUser = new Users("admin@test.com", "password1234", "관리자", "010-1234-5678", UserRole.ADMIN);

		Bid bid = Bid.builder()
			.status(BidStatus.PENDING)
			.type(BidType.BUY) // removeFromRedisZSet에서 type을 체크하므로 추가
			.productOption(productOption)
			.build();
		ReflectionTestUtils.setField(bid, "id", bidId);

		given(bidRepository.findById(bidId)).willReturn(Optional.of(bid));
		given(userRepository.findById(adminId)).willReturn(Optional.of(adminUser));

		// when
		bidService.cancelBidByAdmin(bidId, requestDto, adminId);

		// then
		assertThat(bid.getStatus()).isEqualTo(BidStatus.ADMIN_CANCELED);
		verify(bidRepository, times(1)).findById(bidId);
	}

	/**
	 * 관리자가 아닌 일반 유저 입찰 취소시 예외 발생 시나리오
	 */
	@Test
	@DisplayName("관리자가 아닌 일반 유저가 입찰 취소 시도 시 예외 발생")
	void cancelBidByAdmin_Fail_NotAdmin() {
		// given
		Long bidId = 1L;
		Long userId = 10L;

		Users generalUser = new Users("user@test.com", "pw", "일반", "010-1234-5678", UserRole.USER);
		AdminBidCancelRequestDto requestDto = new AdminBidCancelRequestDto("CANCEL_REASON_01", "취소요청");

		given(userRepository.findById(userId)).willReturn(Optional.of(generalUser));

		// when & then
		assertThatThrownBy(() -> bidService.cancelBidByAdmin(bidId, requestDto, userId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 user 에 권한이 없습니다");
	}
}
