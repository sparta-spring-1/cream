package com.sparta.cream.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sparta.cream.jwt.JwtProperties;
import com.sparta.cream.jwt.JwtTokenProvider;
import com.sparta.cream.security.JwtAuthenticationFilter;
import com.sparta.cream.security.SecurityErrorHandlers;

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
	public SecurityFilterChain filterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		SecurityErrorHandlers securityErrorHandlers) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.httpBasic(httpBasic -> httpBasic.disable())
			.formLogin(formLogin -> formLogin.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(eh -> eh
				.authenticationEntryPoint(securityErrorHandlers)
				.accessDeniedHandler(securityErrorHandlers))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/v1/auth/signup", "/v1/auth/login", "/v1/auth/reissue",
					"/v1/admin/**")
				.permitAll()
				.requestMatchers("/payment-test.html").permitAll()
				.requestMatchers("/actuator/**").permitAll()
				.anyRequest().authenticated());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
		org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
		configuration.addAllowedOriginPattern("http://localhost:*"); // 기존 로컬 포트 허용
		configuration.addAllowedOrigin("http://3.38.101.128"); // EC2 퍼블릭 IP 주소 허용
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
