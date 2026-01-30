package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.entity.Refund;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
