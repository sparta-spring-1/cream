package com.sparta.cream.domain.bid.dto;

import com.sparta.cream.domain.bid.entity.BidType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 입찰 등록 요청을 위한 DTO 클래스입니다.
 * 사용자가 특정 상품 옵션에 대해 입찰을 등록할때 필요한 요청 데이터를
 * 전달하기 위한 객체입니다.
 * BidRequestDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BidRequestDto {

	@NotNull(message = "상품 옵션 ID는 필수입니다.")
	private Long productOptionId;

	@NotNull(message = "입찰 가격은 필수입니다.")
	@Positive(message = "입찰 가격은 0원보다 커야 합니다.")
	private Long price;

	@NotNull(message = "입찰 타입은 필수입니다.")
	private BidType type;
}
