package com.sparta.cream.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

	@InjectMocks
	private SettlementService settlementService;

	@Mock
	private SettlementRepository settlementRepository;

	@Test
	@DisplayName("정산 등록 성공")
	void set_success_with_payments() {
		// given
		Users seller = createMockUser(2L, "seller@email.com", "판매자", UserRole.USER);
		Trade trade1 = createMockTrade(1L, seller);
		Trade trade2 = createMockTrade(2L, seller);
		Payment payment1 = createMockPayment(1L, 1000L, PaymentStatus.PAID_SUCCESS, trade1);
		Payment payment2 = createMockPayment(2L, 2000L, PaymentStatus.PAID_SUCCESS, trade2);
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
		Users seller = createMockUser(2L, "seller@email.com", "판매자", UserRole.USER);
		Trade trade1 = createMockTrade(1L, seller);
		Trade trade2 = createMockTrade(2L, seller);
		Settlement settlement1 = createMockSettlement(1L, 1000L, SettlementStatus.PENDING, trade1);
		Settlement settlement2 = createMockSettlement(2L, 2000L, SettlementStatus.PENDING, trade2);
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
		Users seller = createMockUser(2L, "seller@email.com", "판매자", UserRole.USER);
		Trade trade1 = createMockTrade(1L, seller);
		Settlement settlement1 = createMockSettlement(1L, 1000L, SettlementStatus.COMPLETED, trade1);
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
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);
		Users seller = createMockUser(userId, "seller@email.com", "판매자", UserRole.USER);
		Trade trade = createMockTrade(1L, seller);
		Settlement settlement = createMockSettlement(1L, 1000L, SettlementStatus.COMPLETED, trade);
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
		Long userId = 1L;
		Long settlementId = 1L;
		Users seller = createMockUser(userId, "seller@email.com", "판매자", UserRole.USER);
		Trade trade = createMockTrade(1L, seller);
		Settlement settlement = createMockSettlement(settlementId, 1000L, SettlementStatus.COMPLETED, trade);

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
		Long userId = 1L;
		Long settlementId = 1L;

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
		ProductOption productOption = createMockProductOption(id + 300, product, "270");
		Bid saleBid = createMockBid(id + 100, seller, productOption);
		Trade trade = new Trade(null, saleBid, 50000L);
		ReflectionTestUtils.setField(trade, "id", id);
		return trade;
	}

	private Payment createMockPayment(Long id, Long amount, PaymentStatus status, Trade trade) {
		Payment payment =
			new Payment("merchant-" + id, "Product " + id, amount, status, trade, null);
		ReflectionTestUtils.setField(payment, "id", id);
		ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(payment, "updatedAt", LocalDateTime.now());
		return payment;
	}

	private Settlement createMockSettlement(Long id, Long amount, SettlementStatus status, Trade trade) {
		Payment mockPayment = createMockPayment(id, amount, PaymentStatus.PAID_SUCCESS, trade);
		Settlement settlement = new Settlement(amount, status, mockPayment);
		ReflectionTestUtils.setField(settlement, "id", id);
		ReflectionTestUtils.setField(settlement, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(settlement, "updatedAt", LocalDateTime.now());
		return settlement;
	}
}
