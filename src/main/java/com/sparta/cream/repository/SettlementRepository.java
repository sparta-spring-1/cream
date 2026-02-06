package com.sparta.cream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.SettlementStatus;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByStatus(SettlementStatus status);

    List<Settlement> findAllBySellerId(Long sellerId);

}
