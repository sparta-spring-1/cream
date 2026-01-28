package com.sparta.cream.config;

import com.sparta.cream.jwt.JwtProperties;
import com.sparta.cream.jwt.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;

/**
 * Spring Security 설정 클래스
 * 비밀번호 인코더, JWT 토큰 제공자, 보안 필터 체인을 설정합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

	/**
	 * 비밀번호 인코더 Bean 등록
	 * BCrypt 알고리즘을 사용하여 비밀번호를 암호화합니다.
	 *
	 * @return PasswordEncoder (BCryptPasswordEncoder)
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * JWT 토큰 제공자 Bean 등록
	 *
	 * @param props JWT 설정 프로퍼티
	 * @return JwtTokenProvider
	 */
	@Bean
	public JwtTokenProvider jwtTokenProvider(JwtProperties props) {
		return new JwtTokenProvider(props);
	}

	/**
	 * 보안 필터 체인 설정
	 * CSRF 비활성화, Stateless 세션 관리, 인증 경로 설정을 수행합니다.
	 *
	 * @param http HttpSecurity
	 * @return SecurityFilterChain
	 * @throws Exception 설정 오류
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/v1/auth/signup", "/v1/auth/login", "/v1/auth/reissue").permitAll()
				.anyRequest().authenticated()
			);

		return http.build();
	}
}

