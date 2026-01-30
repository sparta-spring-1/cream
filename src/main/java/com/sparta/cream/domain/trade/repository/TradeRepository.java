package com.sparta.cream.domain.trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.cream.domain.trade.entity.Trade;

/**
 * 거래(Trade) 레포지토리
 * {@link Trade} 엔티티에 대한 데이터베이스 접근 기능을 제공합니다.
 * Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD 및 페이징 기능을 지원합니다.
 * TradeRepository.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, TradeRepositoryCustom {
}
