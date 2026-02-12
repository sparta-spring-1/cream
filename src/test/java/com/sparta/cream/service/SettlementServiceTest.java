package com.sparta.cream.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.domain.status.SettlementStatus;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.dto.response.SettlementDetailsResponse;
import com.sparta.cream.dto.response.SettlementListResponse;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.repository.SettlementRepository;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

	private static final Long SELLER_ID = 1L;
	private static final Long TRADE_ID_1 = 1L;
	private static final Long TRADE_ID_2 = 2L;
	private static final Long PAYMENT_ID_1 = 1L;
	private static final Long PAYMENT_ID_2 = 2L;
	private static final Long SETTLEMENT_ID = 1L;
	private static final BigDecimal AMOUNT_1000 = BigDecimal.valueOf(1000);
	private static final BigDecimal AMOUNT_2000 = BigDecimal.valueOf(2000);
	private static final BigDecimal STANDARD_TRADE_AMOUNT = BigDecimal.valueOf(50000);
	private static final String SELLER_EMAIL = "seller@email.com";
	private static final String SELLER_NAME = "판매자";
	private static final String PRODUCT_SIZE = "270";

	@InjectMocks
	private SettlementService settlementService;
	@Mock
	private SettlementRepository settlementRepository;

	@Test
	@DisplayName("정산 등록 성공")
	void set_success_with_payments() {
		// given
		Users seller = createMockUser(SELLER_ID, SELLER_EMAIL, SELLER_NAME, UserRole.USER);
		Trade trade1 = createMockTrade(TRADE_ID_1, seller);
		Trade trade2 = createMockTrade(TRADE_ID_2, seller);
		Payment payment1 = createMockPayment(PAYMENT_ID_1, AMOUNT_1000, PaymentStatus.PAID_SUCCESS, trade1);
		Payment payment2 = createMockPayment(PAYMENT_ID_2, AMOUNT_2000, PaymentStatus.PAID_SUCCESS, trade2);
		List<Payment> payments = Arrays.asList(payment1, payment2);

		// when
		settlementService.set(payments);

		// then
		verify(settlementRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("정산 미등록 성공 - 결제 내역 없을 경우")
	void set_success_no_payments() {
		// given
		List<Payment> payments = Collections.emptyList();

		// when
		settlementService.set(payments);

		// then
		verify(settlementRepository, never()).saveAll(anyList());
	}

	@Test
	@DisplayName("정산 처리 성공")
	void settle_success_with_pending_settlements() {
		// given
		Users seller = createMockUser(SELLER_ID, SELLER_EMAIL, SELLER_NAME, UserRole.USER);
		Trade trade1 = createMockTrade(TRADE_ID_1, seller);
		Trade trade2 = createMockTrade(TRADE_ID_2, seller);
		Settlement settlement1 = createMockSettlement(SETTLEMENT_ID, AMOUNT_1000, SettlementStatus.PENDING, trade1);
		Settlement settlement2 = createMockSettlement(TRADE_ID_2, AMOUNT_2000, SettlementStatus.PENDING, trade2);
		List<Settlement> settlements = Arrays.asList(settlement1, settlement2);

		// when
		settlementService.settle(settlements);

		// then
		assertEquals(SettlementStatus.COMPLETED, settlement1.getStatus());
		assertEquals(SettlementStatus.COMPLETED, settlement2.getStatus());
		verify(settlementRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("정산 처리 성공 - PENDING 상태 정산이 없는 경우")
	void settle_success_no_pending_settlements() {
		// given
		Users seller = createMockUser(SELLER_ID, SELLER_EMAIL, SELLER_NAME, UserRole.USER);
		Trade trade1 = createMockTrade(TRADE_ID_1, seller);
		Settlement settlement1 = createMockSettlement(SETTLEMENT_ID, AMOUNT_1000, SettlementStatus.COMPLETED, trade1);
		List<Settlement> settlements = Collections.singletonList(settlement1);

		// when
		settlementService.settle(settlements);

		// then
		assertEquals(SettlementStatus.COMPLETED, settlement1.getStatus());
		verify(settlementRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("정산 목록 조회")
	void getSettlements_success() {
		// given
		Long userId = SELLER_ID;
		Pageable pageable = PageRequest.of(0, 10);
		Users seller = createMockUser(userId, SELLER_EMAIL, SELLER_NAME, UserRole.USER);
		Trade trade = createMockTrade(TRADE_ID_1, seller);
		Settlement settlement = createMockSettlement(SETTLEMENT_ID, AMOUNT_1000, SettlementStatus.COMPLETED, trade);
		List<Settlement> settlementList = Collections.singletonList(settlement);
		Page<Settlement> settlementPage = new PageImpl<>(settlementList, pageable, 1);

		given(settlementRepository.findAllSettlementsWithDetailsBySellerId(userId, pageable)).willReturn(
			settlementPage);

		// when
		Page<SettlementListResponse> result = settlementService.getSettlements(userId, pageable);

		// then
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		verify(settlementRepository, times(1))
			.findAllSettlementsWithDetailsBySellerId(userId, pageable);
	}

	@Test
	@DisplayName("정산 상세 조회")
	void getSettlement_success() {
		// given
		Long userId = SELLER_ID;
		Long settlementId = SETTLEMENT_ID;
		Users seller = createMockUser(userId, SELLER_EMAIL, SELLER_NAME, UserRole.USER);
		Trade trade = createMockTrade(TRADE_ID_1, seller);
		Settlement settlement = createMockSettlement(settlementId, AMOUNT_1000, SettlementStatus.COMPLETED, trade);

		given(settlementRepository.findSettlementWithDetailsByIdAndSellerId(settlementId, userId)).willReturn(
			Optional.of(settlement));

		// when
		SettlementDetailsResponse result = settlementService.getSettlement(userId, settlementId);

		// then
		assertNotNull(result);
		assertEquals(settlementId, result.getId());
		verify(settlementRepository, times(1))
			.findSettlementWithDetailsByIdAndSellerId(settlementId, userId);
	}

	@Test
	@DisplayName("정산 상세 조회 실패 - 찾을 수 없음")
	void getSettlement_fail_not_found() {
		// given
		Long userId = SELLER_ID;
		Long settlementId = SETTLEMENT_ID;

		given(settlementRepository.findSettlementWithDetailsByIdAndSellerId(settlementId, userId)).willReturn(
			Optional.empty());

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> settlementService.getSettlement(userId, settlementId));
		assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
		verify(settlementRepository, times(1))
			.findSettlementWithDetailsByIdAndSellerId(settlementId, userId);
	}

	private Users createMockUser(Long id, String email, String name, UserRole role) {
		Users user = new Users(email, "password", name, "010-1234-5678", role);
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private Product createMockProduct(Long id, String name) {
		Product product = Product.builder().name(name).build();
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}

	private ProductOption createMockProductOption(Long id, Product product, String size) {
		ProductOption productOption = ProductOption.builder().product(product).size(size).build();
		ReflectionTestUtils.setField(productOption, "id", id);
		return productOption;
	}

	private Bid createMockBid(Long id, Users user, ProductOption productOption) {
		Bid bid = Bid.builder().user(user).productOption(productOption).build();
		ReflectionTestUtils.setField(bid, "id", id);
		return bid;
	}

	private Trade createMockTrade(Long id, Users seller) {
		Product product = createMockProduct(id + 200, "Test Product " + id);
		ProductOption productOption = createMockProductOption(id + 300, product, PRODUCT_SIZE);
		Bid saleBid = createMockBid(id + 100, seller, productOption);
		Trade trade = new Trade(null, saleBid, STANDARD_TRADE_AMOUNT.longValue());
		ReflectionTestUtils.setField(trade, "id", id);
		return trade;
	}

	private Payment createMockPayment(Long id, BigDecimal amount, PaymentStatus status, Trade trade) {
		Payment payment =
			new Payment("merchant-" + id, "Product " + id, amount, status, trade, null);
		ReflectionTestUtils.setField(payment, "id", id);
		ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(payment, "updatedAt", LocalDateTime.now());
		return payment;
	}

	private Settlement createMockSettlement(Long id, BigDecimal amount, SettlementStatus status, Trade trade) {
		Payment mockPayment = createMockPayment(id, amount, PaymentStatus.PAID_SUCCESS, trade);
		Settlement settlement = new Settlement(amount, status, mockPayment);
		ReflectionTestUtils.setField(settlement, "id", id);
		ReflectionTestUtils.setField(settlement, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(settlement, "updatedAt", LocalDateTime.now());
		return settlement;
	}
}
