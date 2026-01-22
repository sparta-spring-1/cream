package com.sparta.cream.domain.entity.common;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 엔티티의 생성 및 수정 시간을 자동으로 관리하는 추상 클래스입니다.
 * <p>
 * JPA Auditing 기능을 사용하여 엔티티가 생성되고 수정될 때마다
 * `createdAt`과 `updatedAt` 필드를 자동으로 업데이트합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 22.
 */

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	@LastModifiedDate
	private LocalDateTime updatedAt;
}
