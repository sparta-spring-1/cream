package com.sparta.cream.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sparta.cream.config.PortOneConfig;
import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.entity.PaymentHistory;
import com.sparta.cream.domain.entity.Refund;
import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.dto.request.CompletePaymentRequest;
import com.sparta.cream.dto.request.RefundPaymentRequest;
import com.sparta.cream.dto.response.CompletePaymentResponse;
import com.sparta.cream.dto.response.CreatePaymentResponse;
import com.sparta.cream.dto.response.RefundPaymentResponse;
import com.sparta.cream.entity.UserRole;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.PaymentErrorCode;
import com.sparta.cream.repository.PaymentHistoryRepository;
import com.sparta.cream.repository.PaymentRepository;
import com.sparta.cream.repository.RefundRepository;

import lombok.RequiredArgsConstructor;

/**
 * 결제 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 결제 사전 준비(Prepare) 등
 * 결제 프로세스의 전반적인 제어를 담당합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 26.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentHistoryRepository paymentHistoryRepository;
	private final RefundRepository refundRepository;
	private final TradeService tradeService;
	private final AuthService authService;
	private final PortOneConfig portOneConfig;

	/**
	 * 결제 요청을 사전 준비하고 결제 엔티티를 저장합니다.
	 * <p>
	 * 거래(Trade)와 구매자(User) 정보를 바탕으로 merchantUid를 생성하고,
	 * 결제 상태를 READY로 설정하여 DB에 기록합니다.
	 * </p>
	 *
	 * @param tradeId    거래 식별자
	 * @param userId    구매자 식별자
	 * @return 프론트엔드로 전달할 결제 준비 완료 정보(DTO)
	 */
	@Transactional
	public CreatePaymentResponse prepare(Long tradeId, Long userId) {

		Users buyer = authService.findById(userId);

		Trade trade = tradeService.findById(tradeId);

		String merchantUid = "PAY-" + LocalDate.now() + "-" + trade.getId().toString();
		String productName = trade.getPurchaseBidId().getProductOption().getProduct().getName();

		Payment payment = new Payment(merchantUid,
			productName,
			trade.getFinalPrice(),
			PaymentStatus.READY,
			trade,
			buyer);

		paymentRepository.save(payment);

		return new CreatePaymentResponse(
			payment.getId(),
			payment.getMerchantUid(),
			payment.getStatus().toString(),
			productName,
			payment.getAmount(),
			buyer.getEmail(),
			buyer.getName(),
			buyer.getPhoneNumber());
	}

	@Transactional
	public CompletePaymentResponse complete(Long paymentId, CompletePaymentRequest request, Long userId) {
		Payment payment = findById(paymentId);

		payment.changeStatus(payment.getStatus(), PaymentStatus.PENDING);

		if (!payment.getMerchantUid().equals(request.getMerchantUid())) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED);
		}

		if (!payment.getUser().getId().equals(userId)) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED);
		}

		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "PortOne " + portOneConfig.getApiSecret());
			HttpEntity<String> entity = new HttpEntity<>(headers);

			String url = portOneConfig.getBaseUrl() + "/payments/" + request.getMerchantUid() + "?storeId="
				+ portOneConfig.getStoreId();
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

			Map<String, Object> body = response.getBody();
			if (body == null) {
				throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
			}

			Map<String, Object> amountMap = (Map<String, Object>)body.get("amount");
			Number totalAmount = (Number)amountMap.get("total");

			if (!payment.getAmount().equals(totalAmount.longValue())) {
				throw new BusinessException(PaymentErrorCode.PAYMENT_PRICE_MISMATCH);
			}

			Map<String, Object> methodMap = (Map<String, Object>)body.get("method");
			String method = methodMap.get("type").toString();

			PaymentHistory success = payment.completePayment(request.getImpUid(), method, payment.getStatus());
			paymentHistoryRepository.save(success);

			return new CompletePaymentResponse(payment.getImpUid(), payment.getStatus().toString(),
				payment.getPaidAt());

		} catch (RestClientException e) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED);
		}
	}

	@Transactional
	public RefundPaymentResponse refund(Long paymentId, RefundPaymentRequest request, Long userId) {
		Payment payment = findById(paymentId);
		Users user = authService.findById(userId);
		Users seller = authService.findById(payment.getTrade().getSaleBidId().getUser().getId());

		if (!userId.equals(seller.getId()) && !(user.getRole()
			== UserRole.ADMIN)) {
			throw new BusinessException(PaymentErrorCode.UNAUTHORIZED_REFUND);
		}

		if (request.getAmount() > payment.getAmount()) {
			throw new BusinessException(PaymentErrorCode.REFUND_AMOUNT_EXCEEDED);
		}

		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "PortOne " + portOneConfig.getApiSecret());
			headers.set("Content-Type", "application/json");

			String url = portOneConfig.getBaseUrl() + "/payments/" + payment.getMerchantUid() + "/cancel";

			Map<String, Object> requestBody = Map.of(
				"storeId", portOneConfig.getStoreId(),
				"amount", request.getAmount(),
				"reason", request.getReason(),
				"currentCancellableAmount", payment.getAmount(),
				"refundEmail", seller.getEmail()
			);

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

			restTemplate.postForEntity(url, requestEntity, Map.class);

			PaymentHistory history = payment.refund();
			paymentHistoryRepository.save(history);

			Refund refund = new Refund(request.getReason(), request.getAmount(), history);
			refundRepository.save(refund);

			return new RefundPaymentResponse(refund.getId(), refund.getAmount(), payment.getStatus().toString());

		} catch (RestClientException e) {
			throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
		}
	}

	public Payment findById(Long id) {
		return paymentRepository.findById(id).orElseThrow(
			() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
	}
}
