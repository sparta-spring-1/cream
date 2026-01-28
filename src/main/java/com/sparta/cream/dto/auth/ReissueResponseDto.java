package com.sparta.cream.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 재발급 응답 DTO
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReissueResponseDto {

	private String accessToken;
	private String tokenType;
	private long expiresInSec;
}

