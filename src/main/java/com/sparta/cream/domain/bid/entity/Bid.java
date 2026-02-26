package com.sparta.cream.domain.bid.entity;

import java.time.LocalDateTime;

import com.sparta.cream.entity.BaseEntity;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BidErrorCode;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_option_id", nullable = false)
	@NotFound(action = NotFoundAction.IGNORE)
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	private Users canceledByAdmin;

	@Column(length = 100)
	private String adminReason;

	@Column(length = 255)
	private String adminComment;


	public Long getUserId() {
		return this.user != null ? this.user.getId() : null;
	}

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
	public void update(Long price, ProductOption productOption, BidType type) {
		validatePending();
		this.price = price;
		this.productOption = productOption;
		this.type = type;
	}

	/**
	 * 입찰을 취소 상태로 변경합니다.
	 * 다음 조건을 만족하는 경우에만 취소가 가능합니다/
	 * 입팔의 소유자가 요청한 사용자와 일치해야합니다/
	 * 이미 취소된 입찰은 다시 취소할수 없습니다.
	 * 입찰상태가 (PENDING)인 경우에만 취소할수 있습니다.
	 */
	public void cancel(Long userId) {
		if (!this.user.getId().equals(userId)) {
			throw new BusinessException(BidErrorCode.NOT_YOUR_BID);
		}

		if (this.status == BidStatus.CANCELED) {
			throw new BusinessException(BidErrorCode.BID_ALREADY_CANCELED);
		}

		if (this.status != BidStatus.PENDING) {
			throw new BusinessException(BidErrorCode.CANNOT_CANCEL_NON_PENDING_BID);
		}

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
	 * 체결된 거래를 '취소 상태'로 확정합니다. (종료)
	 */
	public void cancelMatchedBid() {
		if (this.status != BidStatus.MATCHED) {
			throw new BusinessException(BidErrorCode.CANNOT_CANCEL_UNMATCHED);
		}
		this.status = BidStatus.CANCELED;
	}

	/**
	 * 현재 입찰이 대기(PENDING) 상태인지 검증합니다.
	 */
	private void validatePending() {
		if (this.status != BidStatus.PENDING) {
			throw new BusinessException(BidErrorCode.CANNOT_UPDATE_BID);
		}
	}

	/**
	 * 관리자 권한으로 입찰을 강제로 취소하고 관련 이력을 기록합니다.
	 * 이미 관리자에 의해 취소된 입찰인 경우 예외가 발생하며.
	 * 취소 시 담당 관리자 ID, 사유 코드, 상세 코멘트가 함계 저장됩니다.
	 * @param admin 취소를 수행한 관리자 식별자
	 * @param reasonCode 취소 사유 코드
	 * @param comment 취소와 관련된 관리자 메모(코멘트)
	 */
	public void cancelByAdmin(Users admin, String reasonCode,  String comment) {
		if (admin.getRole() != com.sparta.cream.entity.UserRole.ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);

		}

		if (this.status == BidStatus.ADMIN_CANCELED) {
			throw new BusinessException(BidErrorCode.BID_ALREADY_CANCELED);
		}

		this.status = BidStatus.ADMIN_CANCELED;
		this.canceledByAdmin = admin;
		this.adminReason = reasonCode;
		this.adminComment = comment;
	}

	/**
	 * 체결된 입찰을 거래 취소로 인해 취소 상태로 변경합니다.
	 */
	public void cancelByTrade() {
		if (this.status != BidStatus.MATCHED) {
			throw new BusinessException(BidErrorCode.CANNOT_CANCEL_UNMATCHED);
		}
		this.status = BidStatus.CANCELED;
	}

	/**
	 * 체결 취소로 인해 상대방 입찰을 다시 대기 상태로 복구합니다.
	 */
	public void restoreToPending() {
		if (this.status != BidStatus.MATCHED) {
			throw new BusinessException(BidErrorCode.INVALID_BID_STATUS);
		}
		this.status = BidStatus.PENDING;
	}

}

