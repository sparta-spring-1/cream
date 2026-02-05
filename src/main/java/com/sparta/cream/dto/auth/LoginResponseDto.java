package com.sparta.cream.dto.auth;

import com.sparta.cream.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

	private String accessToken;
	private String tokenType;
	private long expiresInSec;
	private UserSummary user;

	/**
	 * 사용자 정보 요약 DTO
	 *
	 * @author 오정빈
	 * @version 1.0
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserSummary {

		private Long id;
		private String email;
		private String name;
		private UserRole role;
	}
}
