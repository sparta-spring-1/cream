package com.sparta.cream.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	private String password;
}
