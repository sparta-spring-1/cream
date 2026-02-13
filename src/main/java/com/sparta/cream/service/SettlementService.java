package com.sparta.cream.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.SettlementStatus;
import com.sparta.cream.dto.response.SettlementDetailsResponse;
import com.sparta.cream.dto.response.SettlementListResponse;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.SettlementErrorCode;
import com.sparta.cream.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 정산 데이터의 생성, 상태 변경, 조회 등의 기능을 제공합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Slf4j(topic = "SettlementService")
@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;

	/**
	 * 결제 목록을 기반으로 대기 중인(PENDING) 정산 데이터를 생성합니다.
	 * <p>
	 * PAID_SUCCESS 상태의 Payment 리스트를 받아 각 Payment에 대한 Settlement를 생성하고 저장합니다.
	 * </p>
	 *
	 * @param payments PAID_SUCCESS 상태의 Payment 엔티티 목록
	 */
	@Transactional
	public void set(List<Payment> payments) {
		log.info("[SettlementService] 정산 설정 시작 (대상 결제 건 수: {})", payments.size());
		if (payments.isEmpty()) {
			log.info("[SettlementService] 정산 설정 대상 Payment가 없습니다.");
			return;
		}

		List<Settlement> newSettlements = payments.stream()
			.map(payment -> new Settlement(
				payment.getAmount(),
				SettlementStatus.PENDING,
				payment))
			.toList();

		settlementRepository.saveAll(newSettlements);
		log.info("[SettlementService] 정산 설정 완료 (생성된 정산 건 수: {})", newSettlements.size());
	}

	/**
	 * 대기 중인(PENDING) 정산 데이터를 완료(COMPLETED) 처리합니다.
	 * <p>
	 * PENDING 상태의 Settlement 리스트를 받아 각 Settlement의 상태를 COMPLETED로 변경하고 저장합니다.
	 * </p>
	 *
	 * @param settlements PENDING 상태의 Settlement 엔티티 목록
	 */
	@Transactional
	public void settle(List<Settlement> settlements) {
		log.info("[SettlementService] 정산 처리 시작 (대상 정산 건 수: {})", settlements.size());
		if (settlements.isEmpty()) {
			log.info("[SettlementService] 정산 처리 대상 Settlement가 없습니다.");
			return;
		}

		settlements.forEach(settlement -> {
			if (settlement.getStatus() == SettlementStatus.PENDING) {
				settlement.complete();
			} else {
				log.warn("[SettlementService] PENDING 상태가 아닌 정산 건(ID: {})이 settle 메서드에 전달되었습니다. 현재 상태: {}",
					settlement.getId(), settlement.getStatus());
			}
		});

		settlementRepository.saveAll(settlements);
		log.info("[SettlementService] 정산 처리 완료 (처리된 정산 건 수: {})", settlements.size());
	}

	/**
	 * 특정 사용자의 모든 정산 내역을 페이징 처리하여 조회합니다.
	 *
	 * @param userId   사용자 식별자 (판매자 ID)
	 * @param pageable 페이지네이션 정보
	 * @return 페이징 처리된 SettlementListResponse DTO 목록
	 */
	@Transactional(readOnly = true)
	public Page<SettlementListResponse> getSettlements(Long userId, Pageable pageable) {
		Page<SettlementListResponse> settlementList = settlementRepository.findAllSettlementsWithDetailsBySellerId(userId, pageable)
			.map(SettlementListResponse::from);
		return settlementList;
	}

	/**
	 * 특정 정산의 상세 정보를 조회합니다.
	 *
	 * @param userId       조회 요청 사용자 식별자 (판매자 ID)
	 * @param settlementId 조회할 정산의 식별자
	 * @return SettlementDetailsResponse DTO
	 * @throws BusinessException 해당 정산 정보를 찾을 수 없을 경우
	 */
	@Transactional(readOnly = true)
	public SettlementDetailsResponse getSettlement(Long userId, Long settlementId) {
		Settlement settlement = settlementRepository.findByIdAndUserId(settlementId, userId)
			.orElseThrow(() -> new BusinessException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

		return SettlementDetailsResponse.from(settlement);
	}

	@Transactional
	public void refundedSettlement(Payment payment) {
		Settlement settlement = settlementRepository.findSettlementByPaymentId(payment.getId()).orElseThrow(
			() -> new BusinessException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

		settlement.refundStatus(payment.getStatus(), SettlementStatus.REFUNDED);
		settlementRepository.save(settlement);
	}
}
