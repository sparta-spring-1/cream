package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.entity.PaymentHistory;

/**
 * PaymentHistory 엔티티의 데이터베이스 CRUD 작업을 처리하는 JpaRepository 인터페이스입니다.
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
