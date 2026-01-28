package com.sparta.cream.domain.bid.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리차 입찰 취소 요청을 위한 DTO 클래스입니다.
 * AdminBidCancelRequestDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 27.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AdminBidCancelRequestDto {

	@NotBlank(message = "사유 코드는 필수입니다.")
	private String reasonCode;

	@NotBlank(message = "상세 코멘트를 입력해주세요.")
	private String comment;
}
