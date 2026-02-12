package com.sparta.cream.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.SettlementStatus;

/**
 * 정산 정보(Settlement)에 대한 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * <p>
 * 정산 엔티티의 CRUD 작업을 처리합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByStatus(SettlementStatus status);

    @Query("""
        SELECT s FROM Settlement s
        JOIN FETCH s.seller u
        JOIN FETCH s.payment p
        JOIN FETCH p.trade t
        JOIN FETCH t.saleBidId sb
        JOIN FETCH sb.productOption po
        JOIN FETCH po.product prod
        WHERE u.id = :sellerId
        """)
    Page<Settlement> findAllSettlementsWithDetailsBySellerId(Long sellerId, Pageable pageable);

        @Query("""
            SELECT s FROM Settlement s
            JOIN FETCH s.seller u
            JOIN FETCH s.payment p
            JOIN FETCH p.trade t
            JOIN FETCH t.saleBidId sb
            JOIN FETCH sb.productOption po
            JOIN FETCH po.product prod
            WHERE s.id = :id AND u.id = :sellerId
            """)
        Optional<Settlement> findSettlementWithDetailsByIdAndSellerId(Long id, Long sellerId);
}
