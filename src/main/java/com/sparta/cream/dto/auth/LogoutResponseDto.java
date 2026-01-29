package com.sparta.cream.dto.auth;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그아웃 응답 DTO
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponseDto {

	private LocalDateTime loggedOutAt;
}

