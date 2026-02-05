package com.sparta.cream.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$",
		message = "비밀번호는 영문자와 숫자가 모두 포함되도록 입력해주세요."
	)
	private String password;

	@NotBlank
	@Size(max = 50)
	private String name;

	@NotBlank
	@Size(max = 20)
	private String phoneNumber;
}

