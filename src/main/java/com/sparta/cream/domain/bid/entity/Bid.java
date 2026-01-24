package com.sparta.cream.domain.bid.entity;

import java.time.LocalDateTime;

import com.sparta.cream.entity.BaseEntity;
import com.sparta.cream.entity.ProductOption;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 입찰(Bid) 도메인 엔티티
 * 구매 또는 판매를 희망하는 사용자의 입찰 정보를 관리합니다.
 * 입찰은 항상 특정 상품의 옵션(상품의 사이즈등)에 귀속되며
 * 낙관적 락(@Version)을 통해 동시성 제어를 수행합니다.
 * Bid.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bids")
public class Bid extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_option_id", nullable = false)
	private ProductOption productOption;

	@Column(nullable = false)
	private Long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BidStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BidType type;

	@Version
	private Long version;

	@Column(nullable = false, updatable = false)
	private LocalDateTime expiresAt;

	/**
	 * 연관된 상품(Product)의 식별자를 조회합니다
	 * @return 상품 식별자(연관정보가 없을 경우 null)
	 */
	public Long getProductId() {
		return (this.productOption != null && this.productOption.getProduct() != null)
			? this.productOption.getProduct().getId()
			: null;
	}

	/**
	 * 입찰 가격 및 옵션 정보를 수정합니다.
	 * @param price 새로운 입찰가격
	 * @param productOption 새로운 상품 옵션
	 */
	public void update(Long price, ProductOption productOption) {
		validatePending();
		this.price = price;
		this.productOption = productOption;
	}

	/**
	 * 입찰을 취소 상태로 변경합니다.
	 */
	public void cancel() {
		validatePending();
		this.status = BidStatus.CANCELED;
	}

	/**
	 * 거래가 채결되어 입찰을 체결 완료 상태로 변경합니다.
	 */
	public void match() {
		validatePending();
		this.status = BidStatus.MATCHED;
	}

	/**
	 * 채결된 거래를 취소하고 다시 대기(PENDING)상태로 되돌립니다.
	 */
	public void undoMatch() {
		if (this.status != BidStatus.MATCHED) {
			throw new IllegalStateException("MATCHED 상태의 입찰만 되돌릴 수 있습니다.");
		}
		this.status = BidStatus.PENDING;
	}

	/**
	 * 현재 입찰이 대기(PENDING) 상태인지 검증합니다.
	 */
	private void validatePending() {
		if (this.status != BidStatus.PENDING) {
			throw new IllegalStateException("PENDING 상태에서만 수행할 수 있는 작업입니다.");
		}
	}
}

