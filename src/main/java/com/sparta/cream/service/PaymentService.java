package com.sparta.cream.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.sparta.cream.client.PortOneApiClient;
import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.entity.PaymentHistory;
import com.sparta.cream.domain.entity.Refund;
import com.sparta.cream.domain.event.PaymentCompletedEvent;
import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.dto.portone.PortOnePaymentResponse;
import com.sparta.cream.dto.request.CompletePaymentRequest;
import com.sparta.cream.dto.request.RefundPaymentRequest;
import com.sparta.cream.dto.response.CompletePaymentResponse;
import com.sparta.cream.dto.response.CreatePaymentResponse;
import com.sparta.cream.dto.response.PaymentDetailsResponse;
import com.sparta.cream.dto.response.RefundPaymentResponse;
import com.sparta.cream.dto.response.YourPaymentListResponse;
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
 * 결제 사전 준비(Prepare), 검증(complete), 환불(refund), 조회 등
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

	private final ApplicationEventPublisher eventPublisher;
	private final TradeService tradeService;
	private final AuthService authService;
	private final SettlementService settlementService;
	private final PortOneApiClient portOneApiClient;

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

		Optional<Payment> existingPayment = paymentRepository.findReadyPaymentByUserIdAndTradeIdAndStatus(userId, tradeId, PaymentStatus.READY);

		if (existingPayment.isPresent()) {
			Payment payment = existingPayment.get();

			return new CreatePaymentResponse(
				payment.getId(),
				payment.getMerchantUid(),
				payment.getStatus().toString(),
				payment.getProductName(),
				payment.getAmount(),
				payment.getUser().getEmail(),
				payment.getUser().getName(),
				payment.getUser().getPhoneNumber());
		}

		Users buyer = authService.findById(userId);
		Trade trade = tradeService.findById(tradeId);

		String merchantUid = "PAY-" + LocalDate.now() + "-" + trade.getId().toString();
		String productName = trade.getPurchaseBidId().getProductOption().getProduct().getName();

		Payment newPayment = new Payment(merchantUid,
			productName,
			BigDecimal.valueOf(trade.getFinalPrice()),
			PaymentStatus.READY,
			trade,
			buyer);

		paymentRepository.save(newPayment);

		return new CreatePaymentResponse(
			newPayment.getId(),
			newPayment.getMerchantUid(),
			newPayment.getStatus().toString(),
			productName,
			newPayment.getAmount(),
			buyer.getEmail(),
			buyer.getName(),
			buyer.getPhoneNumber());
	}

	/**
	 * 결제를 완료 처리하고 검증합니다.
	 * <p>
	 * PortOne API를 통해 결제 정보를 확인하고, DB에 저장된 결제 정보와 일치하는지 검증합니다.
	 * 결제 성공 시 결제 상태를 PAID_SUCCESS로 업데이트하고 알림을 생성합니다.
	 * </p>
	 *
	 * @param paymentId 내부 DB 결제 식별자
	 * @param request   결제 완료 요청 정보 (impUid, merchantUid)
	 * @param userId    결제를 시도한 사용자 식별자
	 * @return 결제 완료 응답 DTO
	 * @throws BusinessException 결제 검증 실패, 금액 불일치, PortOne API 오류 발생 시
	 */
	@Transactional
	public CompletePaymentResponse complete(Long paymentId, CompletePaymentRequest request, Long userId) {
		Payment payment = findById(paymentId);

		if (!payment.getMerchantUid().equals(request.getMerchantUid())) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED);
		}

		if (!payment.getUser().getId().equals(userId)) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED);
		}

		try {
			payment.changeStatus(payment.getStatus(), PaymentStatus.PENDING);

			PortOnePaymentResponse body = portOneApiClient.getPayment(request.getMerchantUid());
			BigDecimal total = new BigDecimal(body.getAmount().getTotal());

			if (body == null) {
				throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
			}

			if (!payment.getAmount().equals(total)) {
				throw new BusinessException(PaymentErrorCode.PAYMENT_PRICE_MISMATCH);
			}

			String method = body.getMethod().getType();

			PaymentHistory success = payment.completePayment(request.getImpUid(), method, payment.getStatus());
			paymentHistoryRepository.save(success);

			eventPublisher.publishEvent(new PaymentCompletedEvent(
				userId,
				payment.getProductName(),
				payment.getAmount().longValue(),
				payment.getId()
			));


			return new CompletePaymentResponse(payment.getImpUid(), payment.getStatus().toString(),
				payment.getPaidAt());

		} catch (RestClientException e) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED);
		}
	}

	/**
	 * 결제를 환불 처리합니다.
	 * <p>
	 * 환불 요청자와 결제 판매자 또는 관리자 권한을 확인하고,
	 * 환불 금액이 기존 결제 금액을 초과하는지 검증합니다.
	 * PortOne API를 통해 실제 환불을 처리하고, DB에 환불 내역을 기록합니다.
	 * </p>
	 *
	 * @param paymentId 내부 DB 결제 식별자
	 * @param request   환불 요청 정보 (환불 사유, 금액)
	 * @param userId    환불을 요청한 사용자 식별자
	 * @return 환불 응답 DTO
	 * @throws BusinessException 권한 없음, 금액 초과, PortOne API 오류 발생 시
	 */
	@Transactional
	public RefundPaymentResponse refund(Long paymentId, RefundPaymentRequest request, Long userId) {
		Payment payment = findById(paymentId);
		Users user = authService.findById(userId);
		Users seller = authService.findById(payment.getTrade().getSaleBidId().getUser().getId());

		if (!userId.equals(seller.getId()) && !(user.getRole()
			== UserRole.ADMIN)) {
			throw new BusinessException(PaymentErrorCode.UNAUTHORIZED_REFUND);
		}

		if (request.getAmount().compareTo(payment.getAmount()) > 0) {
			throw new BusinessException(PaymentErrorCode.REFUND_AMOUNT_EXCEEDED);
		}

		try {
			portOneApiClient.cancelPayment(payment.getMerchantUid(), request.getAmount(), request.getReason(),
				payment.getAmount(), seller.getEmail());

			PaymentHistory history = payment.refund();
			paymentHistoryRepository.save(history);

			Refund refund = new Refund(request.getReason(), request.getAmount(), history);
			refundRepository.save(refund);

			settlementService.refundedSettlement(payment);

			return new RefundPaymentResponse(refund.getId(), refund.getAmount(), payment.getStatus().toString());

		} catch (RestClientException e) {
			throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
		}
	}

	/**
	 * 주어진 ID로 Payment 엔티티를 조회합니다.
	 *
	 * @param id 조회할 Payment의 식별자
	 * @return 조회된 Payment 엔티티
	 * @throws BusinessException 해당 ID의 Payment를 찾을 수 없을 경우
	 */
	public Payment findById(Long id) {
		return paymentRepository.findById(id).orElseThrow(
			() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
	}

	/**
	 * 주어진 상태의 Payment 엔티티 목록을 조회합니다.
	 *
	 * @param status 조회할 결제 상태
	 * @return 해당 상태의 Payment 엔티티 목록
	 */
	public List<Payment> getByStatus(PaymentStatus status) {
		return paymentRepository.findByStatus(status);
	}

	/**
	 * 특정 사용자의 모든 결제 내역을 페이징 처리하여 조회합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param pageable 페이지네이션 정보
	 * @return 페이징 처리된 YourPaymentListResponse DTO 목록
	 */
	@Transactional(readOnly = true)
	public Page<YourPaymentListResponse> getAllPayment(Long userId, Pageable pageable) {
		Page<YourPaymentListResponse> paymentList = paymentRepository.findAllByUserId(userId, pageable)
			.map(YourPaymentListResponse::from);

		return paymentList;
	}

	/**
	 * 특정 결제의 상세 정보를 조회합니다.
	 *
	 * @param paymentId 조회할 결제의 식별자
	 * @param userId    조회 요청 사용자 식별자
	 * @return PaymentDetailsResponse DTO
	 * @throws BusinessException 해당 결제 정보를 찾을 수 없을 경우
	 */
	@Transactional(readOnly = true)
	public PaymentDetailsResponse getDetails(Long paymentId, Long userId) {
		Payment payment = paymentRepository.findPaymentWithUserByIdAndUserId(paymentId, userId)
			.orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		return PaymentDetailsResponse.from(payment);
	}
}
