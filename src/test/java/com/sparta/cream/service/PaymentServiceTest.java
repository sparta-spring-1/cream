package com.sparta.cream.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.cream.client.PortOneApiClient;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.notification.service.NotificationService;
import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.dto.portone.PortOnePaymentResponse;
import com.sparta.cream.dto.request.CompletePaymentRequest;
import com.sparta.cream.dto.request.RefundPaymentRequest;
import com.sparta.cream.dto.response.CompletePaymentResponse;
import com.sparta.cream.dto.response.CreatePaymentResponse;
import com.sparta.cream.dto.response.RefundPaymentResponse;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.PaymentErrorCode;
import com.sparta.cream.repository.PaymentHistoryRepository;
import com.sparta.cream.repository.PaymentRepository;
import com.sparta.cream.repository.RefundRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	private static final Long BUYER_ID = 10L;
	private static final Long SELLER_ID = 100L;
	private static final Long OTHER_USER_ID = 200L;
	private static final Long PAYMENT_ID = 300L;
	private static final Long TRADE_ID = 1L;
	private static final BigDecimal STANDARD_AMOUNT = BigDecimal.valueOf(50000);
	private static final BigDecimal LOWER_AMOUNT = BigDecimal.valueOf(40000);
	private static final BigDecimal HIGHER_AMOUNT = BigDecimal.valueOf(60000);
	private static final String MERCHANT_UID = "PAY-123";
	private static final String DIFFERENT_MERCHANT_UID = "PAY-DIFF";
	private static final String IMP_UID = "IMP-123";

	@InjectMocks
	private PaymentService paymentService;
	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private PaymentHistoryRepository paymentHistoryRepository;
	@Mock
	private RefundRepository refundRepository;
	@Mock
	private NotificationService notificationService;
	@Mock
	private TradeService tradeService;
	@Mock
	private AuthService authService;
	@Mock
	private PortOneApiClient portOneApiClient;

	@Test
	@DisplayName("결제 사전 준비 성공 - 신규 요청")
	void prepare_success() {
		// given
		Users buyer = createMockUser(BUYER_ID, "buyer@email.com", "구매자", UserRole.USER);
		Trade trade = createMockTrade(TRADE_ID, STANDARD_AMOUNT, null);

		given(paymentRepository.findReadyPaymentByUserIdAndTradeIdAndStatus(
			BUYER_ID, TRADE_ID, PaymentStatus.READY))
			.willReturn(Optional.empty());

		given(authService.findById(BUYER_ID)).willReturn(buyer);
		given(tradeService.findById(TRADE_ID)).willReturn(trade);
		given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
			Payment p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", 123L);
			return p;
		});

		// when
		CreatePaymentResponse response = paymentService.prepare(TRADE_ID, BUYER_ID);

		// then
		assertNotNull(response);
		assertEquals("Nike Shoes", response.getProductName());
		assertEquals(STANDARD_AMOUNT, response.getAmount());
		assertEquals(buyer.getEmail(), response.getEmail());
		assertEquals("READY", response.getStatus());
		verify(paymentRepository, times(1)).save(any(Payment.class));
	}

	@Test
	@DisplayName("결제 사전 준비 성공 - 기존 READY 상태 결제 존재")
	void prepare_success_existing_ready_payment() {
		// given
		Users buyer = createMockUser(BUYER_ID, "buyer@email.com", "구매자", UserRole.USER);
		Trade trade = createMockTrade(TRADE_ID, STANDARD_AMOUNT, null);
		Payment existingPayment = createMockPayment(PAYMENT_ID, MERCHANT_UID, STANDARD_AMOUNT,
			PaymentStatus.READY, buyer, trade);

		given(paymentRepository.findReadyPaymentByUserIdAndTradeIdAndStatus(
			BUYER_ID, TRADE_ID, PaymentStatus.READY))
			.willReturn(Optional.of(existingPayment));

		// when
		CreatePaymentResponse response = paymentService.prepare(TRADE_ID, BUYER_ID);

		// then
		assertNotNull(response);
		assertEquals(PAYMENT_ID, response.getId());
		assertEquals(MERCHANT_UID, response.getPaymentId());
		assertEquals("READY", response.getStatus());
		assertEquals("Nike Shoes", response.getProductName());
		assertEquals(STANDARD_AMOUNT, response.getAmount());
		assertEquals(buyer.getEmail(), response.getEmail());

		verify(paymentRepository, never()).save(any(Payment.class));
		verify(authService, never()).findById(any());
		verify(tradeService, never()).findById(any());
	}

	@Test
	@DisplayName("결제 완료 검증 성공")
	void complete_success() {
		// given
		Users buyer = createMockUser(BUYER_ID, "buyer@email.com", "Buyer", UserRole.USER);
		Payment payment = createMockPayment(PAYMENT_ID, MERCHANT_UID, STANDARD_AMOUNT, PaymentStatus.READY, buyer, null);

		CompletePaymentRequest request = createCompletePaymentRequest(MERCHANT_UID, IMP_UID);
		PortOnePaymentResponse portOneResponse = createPortOnePaymentResponse(STANDARD_AMOUNT);

		given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
		given(portOneApiClient.getPayment(MERCHANT_UID)).willReturn(portOneResponse);

		// when
		CompletePaymentResponse response = paymentService.complete(PAYMENT_ID, request, BUYER_ID);

		// then
		assertNotNull(response);
		assertEquals("PAID_SUCCESS", response.getStatus());
		assertEquals(IMP_UID, response.getPaymentId());
		verify(paymentHistoryRepository, times(1)).save(any());
		verify(notificationService, times(1)).createNotification(eq(BUYER_ID), anyString());
	}

	@Test
	@DisplayName("결제 완료 실패 - 결제 정보 불일치 (MerchantUid)")
	void complete_fail_merchant_uid_mismatch() {
		// given
		Users buyer = createMockUser(BUYER_ID, "buyer@email.com", "Buyer", UserRole.USER);
		Payment payment = createMockPayment(PAYMENT_ID, MERCHANT_UID, STANDARD_AMOUNT, PaymentStatus.READY, buyer, null);

		CompletePaymentRequest request = createCompletePaymentRequest(DIFFERENT_MERCHANT_UID, null);

		given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.complete(PAYMENT_ID, request, BUYER_ID));
		assertEquals(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED, ex.getErrorCode());
	}

	@Test
	@DisplayName("결제 완료 실패 - 결제 금액 불일치 (Amount)")
	void complete_fail_amount_mismatch() {
		// given
		Users buyer = createMockUser(BUYER_ID, "buyer@email.com", "Buyer", UserRole.USER);
		Payment payment = createMockPayment(PAYMENT_ID, MERCHANT_UID, STANDARD_AMOUNT, PaymentStatus.READY, buyer, null);

		CompletePaymentRequest request = createCompletePaymentRequest(MERCHANT_UID, null);
		PortOnePaymentResponse portOneResponse = createPortOnePaymentResponse(LOWER_AMOUNT);

		given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
		given(portOneApiClient.getPayment(MERCHANT_UID)).willReturn(portOneResponse);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.complete(PAYMENT_ID, request, BUYER_ID));
		assertEquals(PaymentErrorCode.PAYMENT_PRICE_MISMATCH, ex.getErrorCode());
	}

	@Test
	@DisplayName("환불 성공")
	void refund_success() {
		// given
		Users seller = createMockUser(SELLER_ID, "seller@email.com", "Seller", UserRole.USER);
		Bid saleBid = Bid.builder().user(seller).build();
		Trade trade = new Trade(null, saleBid, STANDARD_AMOUNT.longValue());

		Payment payment = createMockPayment(PAYMENT_ID, MERCHANT_UID, STANDARD_AMOUNT, PaymentStatus.PAID_SUCCESS, null, trade);

		RefundPaymentRequest request = new RefundPaymentRequest(1L, "Out of stock", STANDARD_AMOUNT);

		given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
		given(authService.findById(SELLER_ID)).willReturn(seller);

		// when
		RefundPaymentResponse response = paymentService.refund(PAYMENT_ID, request, SELLER_ID);

		// then
		assertNotNull(response);
		assertEquals("FULL_REFUNDED", response.getStatus());
		assertEquals(STANDARD_AMOUNT, response.getCancelledAmount());
		verify(portOneApiClient, times(1)).cancelPayment(eq(MERCHANT_UID), eq(STANDARD_AMOUNT), anyString(), eq(STANDARD_AMOUNT),
			eq(seller.getEmail()));
		verify(paymentHistoryRepository, times(1)).save(any());
		verify(refundRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("환불 실패 - 권한 없음 (판매자도 아니고 관리자도 아님)")
	void refund_fail_unauthorized() {
		// given
		Users otherUser = createMockUser(OTHER_USER_ID, "other@email.com", "Other", UserRole.USER);
		Users seller = createMockUser(SELLER_ID, "seller@email.com", "Seller", UserRole.USER);

		Bid saleBid = Bid.builder().user(seller).build();
		Trade trade = new Trade(null, saleBid, STANDARD_AMOUNT.longValue());

		Payment payment = createMockPayment(PAYMENT_ID, "PAY-123", STANDARD_AMOUNT, PaymentStatus.PAID_SUCCESS, null, trade);

		given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
		given(authService.findById(OTHER_USER_ID)).willReturn(otherUser);
		given(authService.findById(SELLER_ID)).willReturn(seller);

		RefundPaymentRequest request = new RefundPaymentRequest(1L, "Reason", BigDecimal.valueOf(50000));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.refund(PAYMENT_ID, request, OTHER_USER_ID));
		assertEquals(PaymentErrorCode.UNAUTHORIZED_REFUND, ex.getErrorCode());
	}

	@Test
	@DisplayName("환불 실패 - 환불 금액 초과")
	void refund_fail_amount_exceeded() {
		// given
		Users seller = createMockUser(SELLER_ID, "seller@email.com", "Seller", UserRole.USER);
		Bid saleBid = Bid.builder().user(seller).build();
		Trade trade = new Trade(null, saleBid, STANDARD_AMOUNT.longValue());

		Payment payment = createMockPayment(PAYMENT_ID, "PAY-123", STANDARD_AMOUNT, PaymentStatus.PAID_SUCCESS, null, trade);

		given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
		given(authService.findById(SELLER_ID)).willReturn(seller);

		RefundPaymentRequest request = new RefundPaymentRequest(1L, "Reason", HIGHER_AMOUNT);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.refund(PAYMENT_ID, request, SELLER_ID));
		assertEquals(PaymentErrorCode.REFUND_AMOUNT_EXCEEDED, ex.getErrorCode());
	}

	private Users createMockUser(Long id, String email, String name, UserRole role) {
		Users user = new Users(email, "password", name, "010-1234-5678", role);
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private Trade createMockTrade(Long id, BigDecimal price, Bid purchaseBid) {
		if (purchaseBid == null) {
			Product product = Product.builder().name("Nike Shoes").build();
			ProductOption option = ProductOption.builder().product(product).size("270").build();
			purchaseBid = Bid.builder().productOption(option).build();
		}
		Trade trade = new Trade(purchaseBid, null, price.longValue());
		ReflectionTestUtils.setField(trade, "id", id);
		return trade;
	}

	private Payment createMockPayment(Long id, String merchantUid, BigDecimal amount, PaymentStatus status, Users user,
		Trade trade) {
		Payment payment = new Payment(merchantUid, "Nike Shoes", amount, status, trade, user);
		ReflectionTestUtils.setField(payment, "id", id);
		return payment;
	}

	private CompletePaymentRequest createCompletePaymentRequest(String merchantUid, String impUid) {
		CompletePaymentRequest request = new CompletePaymentRequest();
		ReflectionTestUtils.setField(request, "merchantUid", merchantUid);
		ReflectionTestUtils.setField(request, "impUid", impUid);
		return request;
	}

	private PortOnePaymentResponse createPortOnePaymentResponse(BigDecimal amount) {
		PortOnePaymentResponse response = new PortOnePaymentResponse();
		PortOnePaymentResponse.Amount amountObj = new PortOnePaymentResponse.Amount();
		ReflectionTestUtils.setField(amountObj, "total", amount.intValue());
		PortOnePaymentResponse.Method methodObj = new PortOnePaymentResponse.Method();
		ReflectionTestUtils.setField(methodObj, "type", "CARD");

		ReflectionTestUtils.setField(response, "amount", amountObj);
		ReflectionTestUtils.setField(response, "method", methodObj);
		return response;
	}
}
