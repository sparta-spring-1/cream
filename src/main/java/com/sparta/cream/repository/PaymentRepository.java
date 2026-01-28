package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.entity.Payment;

/**
 * 결제 정보(Payment)에 대한 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * <p>
 * 결제 엔티티의 CRUD 작업을 처리합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 26.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	// boolean existsByTradeId(Long tradeId);
	// Optional<Payment> findByTradeId(Long tradeId);
}
