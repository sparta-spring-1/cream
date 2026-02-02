package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.entity.PaymentHistory;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
