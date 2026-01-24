package com.sparta.cream.domain.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.bid.entity.Bid;

/**
 * 입찰(Bid) 도메인을 위한 데이터 접근 저장소입니다.
 * BidRepository.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
public interface BidRepository extends JpaRepository<Bid, Long> {

}
