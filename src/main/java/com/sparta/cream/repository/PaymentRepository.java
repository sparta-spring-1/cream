package com.sparta.cream.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.cream.domain.entity.Payment;
import com.sparta.cream.domain.status.PaymentStatus;

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

    List<Payment> findByStatus(PaymentStatus status);
	Page<Payment> findAllByUserId(Long userId, Pageable pageable);

	@Query("SELECT p FROM Payment p JOIN FETCH p.user u WHERE p.id = :id AND u.id = :userId")
	Optional<Payment> findPaymentWithUserByIdAndUserId(Long id, Long userId);
}

