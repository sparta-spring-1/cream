package com.sparta.cream.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;

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
	@DisplayName("결제 사전 준비 성공")
	void prepare_success() {
		// given
		Long tradeId = 1L;
		Long userId = 10L;
		Long price = 50000L;

		Users buyer = createMockUser(userId, "buyer@email.com", "구매자", UserRole.USER);
		Trade trade = createMockTrade(tradeId, price, null);

		given(authService.findById(userId)).willReturn(buyer);
		given(tradeService.findById(tradeId)).willReturn(trade);
		given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
			Payment p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", 123L);
			return p;
		});

		// when
		CreatePaymentResponse response = paymentService.prepare(tradeId, userId);

		// then
		assertNotNull(response);
		assertEquals("Nike Shoes", response.getProductName());
		assertEquals(price, response.getAmount());
		assertEquals(buyer.getEmail(), response.getEmail());
		assertEquals("READY", response.getStatus());
		verify(paymentRepository, times(1)).save(any(Payment.class));
	}

	@Test
	@DisplayName("결제 완료 검증 성공")
	void complete_success() {
		// given
		Long paymentId = 123L;
		Long userId = 10L;
		Long amount = 50000L;
		String merchantUid = "PAY-123";
		String impUid = "IMP-123";

		Users buyer = createMockUser(userId, "buyer@email.com", "Buyer", UserRole.USER);
		Payment payment = createMockPayment(paymentId, merchantUid, amount, PaymentStatus.READY, buyer, null);

		CompletePaymentRequest request = createCompletePaymentRequest(merchantUid, impUid);
		PortOnePaymentResponse portOneResponse = createPortOnePaymentResponse(amount);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
		given(portOneApiClient.getPayment(merchantUid)).willReturn(portOneResponse);

		// when
		CompletePaymentResponse response = paymentService.complete(paymentId, request, userId);

		// then
		assertNotNull(response);
		assertEquals("PAID_SUCCESS", response.getStatus());
		assertEquals(impUid, response.getPaymentId());
		verify(paymentHistoryRepository, times(1)).save(any());
		verify(notificationService, times(1)).createNotification(eq(userId), anyString());
	}

	@Test
	@DisplayName("결제 완료 실패 - 결제 정보 불일치 (MerchantUid)")
	void complete_fail_merchant_uid_mismatch() {
		// given
		Long paymentId = 123L;
		Long userId = 100L;

		Users buyer = createMockUser(userId, "buyer@email.com", "Buyer", UserRole.USER);
		Payment payment = createMockPayment(paymentId, "PAY-ORIGINAL", 50000L, PaymentStatus.READY, buyer, null);

		CompletePaymentRequest request = createCompletePaymentRequest("PAY-DIFF", null);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.complete(paymentId, request, userId));
		assertEquals(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED, ex.getErrorCode());
	}

	@Test
	@DisplayName("결제 완료 실패 - 결제 금액 불일치 (Amount)")
	void complete_fail_amount_mismatch() {
		// given
		Long paymentId = 123L;
		Long userId = 100L;
		Long amount = 50000L;
		String merchantUid = "PAY-123";

		Users buyer = createMockUser(userId, "buyer@email.com", "Buyer", UserRole.USER);
		Payment payment = createMockPayment(paymentId, merchantUid, amount, PaymentStatus.READY, buyer, null);

		CompletePaymentRequest request = createCompletePaymentRequest(merchantUid, null);
		PortOnePaymentResponse portOneResponse = createPortOnePaymentResponse(40000L);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
		given(portOneApiClient.getPayment(merchantUid)).willReturn(portOneResponse);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.complete(paymentId, request, userId));
		assertEquals(PaymentErrorCode.PAYMENT_PRICE_MISMATCH, ex.getErrorCode());
	}

	@Test
	@DisplayName("환불 성공")
	void refund_success() {
		// given
		Long paymentId = 123L;
		Long userId = 100L;
		Long sellerId = 100L;
		Long amount = 50000L;
		String merchantUid = "PAY-123";

		Users seller = createMockUser(sellerId, "seller@email.com", "Seller", UserRole.USER);
		Bid saleBid = Bid.builder().user(seller).build();
		Trade trade = new Trade(null, saleBid, amount);

		Payment payment = createMockPayment(paymentId, merchantUid, amount, PaymentStatus.PAID_SUCCESS, null, trade);

		RefundPaymentRequest request = new RefundPaymentRequest(1L, "Out of stock", amount);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
		given(authService.findById(userId)).willReturn(seller);
		given(authService.findById(sellerId)).willReturn(seller);

		// when
		RefundPaymentResponse response = paymentService.refund(paymentId, request, userId);

		// then
		assertNotNull(response);
		assertEquals("FULL_REFUNDED", response.getStatus());
		assertEquals(amount, response.getCancelledAmount());
		verify(portOneApiClient, times(1)).cancelPayment(eq(merchantUid), eq(amount), anyString(), eq(amount),
			eq(seller.getEmail()));
		verify(paymentHistoryRepository, times(1)).save(any());
		verify(refundRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("환불 실패 - 권한 없음 (판매자도 아니고 관리자도 아님)")
	void refund_fail_unauthorized() {
		// given
		Long paymentId = 123L;
		Long userId = 200L;
		Long sellerId = 100L;

		Users otherUser = createMockUser(userId, "other@email.com", "Other", UserRole.USER);
		Users seller = createMockUser(sellerId, "seller@email.com", "Seller", UserRole.USER);

		Bid saleBid = Bid.builder().user(seller).build();
		Trade trade = new Trade(null, saleBid, 50000L);

		Payment payment = createMockPayment(paymentId, "PAY-123", 50000L, PaymentStatus.PAID_SUCCESS, null, trade);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
		given(authService.findById(userId)).willReturn(otherUser);
		given(authService.findById(sellerId)).willReturn(seller);

		RefundPaymentRequest request = new RefundPaymentRequest(1L, "Reason", 50000L);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.refund(paymentId, request, userId));
		assertEquals(PaymentErrorCode.UNAUTHORIZED_REFUND, ex.getErrorCode());
	}

	@Test
	@DisplayName("환불 실패 - 환불 금액 초과")
	void refund_fail_amount_exceeded() {
		// given
		Long paymentId = 123L;
		Long userId = 100L;
		Long amount = 50000L;

		Users seller = createMockUser(userId, "seller@email.com", "Seller", UserRole.USER);
		Bid saleBid = Bid.builder().user(seller).build();
		Trade trade = new Trade(null, saleBid, amount);

		Payment payment = createMockPayment(paymentId, "PAY-123", amount, PaymentStatus.PAID_SUCCESS, null, trade);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
		given(authService.findById(userId)).willReturn(seller);

		RefundPaymentRequest request = new RefundPaymentRequest(1L, "Reason", 60000L);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> paymentService.refund(paymentId, request, userId));
		assertEquals(PaymentErrorCode.REFUND_AMOUNT_EXCEEDED, ex.getErrorCode());
	}

	private Users createMockUser(Long id, String email, String name, UserRole role) {
		Users user = new Users(email, "password", name, "010-1234-5678", role);
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private Trade createMockTrade(Long id, Long price, Bid purchaseBid) {
		if (purchaseBid == null) {
			Product product = Product.builder().name("Nike Shoes").build();
			ProductOption option = ProductOption.builder().product(product).size("270").build();
			purchaseBid = Bid.builder().productOption(option).build();
		}
		Trade trade = new Trade(purchaseBid, null, price);
		ReflectionTestUtils.setField(trade, "id", id);
		return trade;
	}

	private Payment createMockPayment(Long id, String merchantUid, Long amount, PaymentStatus status, Users user,
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

	private PortOnePaymentResponse createPortOnePaymentResponse(Long amount) {
		PortOnePaymentResponse response = new PortOnePaymentResponse();
		PortOnePaymentResponse.Amount amountObj = new PortOnePaymentResponse.Amount();
		ReflectionTestUtils.setField(amountObj, "total", amount);
		PortOnePaymentResponse.Method methodObj = new PortOnePaymentResponse.Method();
		ReflectionTestUtils.setField(methodObj, "type", "CARD");

		ReflectionTestUtils.setField(response, "amount", amountObj);
		ReflectionTestUtils.setField(response, "method", methodObj);
		return response;
	}
}
