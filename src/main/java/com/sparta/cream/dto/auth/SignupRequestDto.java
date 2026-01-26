package com.sparta.cream.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	@Size(min = 8, max = 72)
	private String password;

	@NotBlank
	@Size(max = 50)
	private String name;
}

