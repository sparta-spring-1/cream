package com.sparta.cream.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 * users 테이블과 매핑되는 JPA 엔티티입니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Entity
@NoArgsConstructor
@Getter
public class Users extends BaseEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 255)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Column(name = "bid_blocked_until")
	private LocalDateTime bidBlockedUntil;


	/**
	 * 사용자 생성자
	 * 기본 역할은 USER로 설정됩니다.
	 *
	 * @param email 사용자 이메일
	 * @param password 암호화된 비밀번호
	 * @param name 사용자 이름
	 */
	public Users(String email, String password, String name) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.role = UserRole.USER;
	}

	/**
	 * 사용자 생성자 (역할 지정)
	 *
	 * @param email 사용자 이메일
	 * @param password 암호화된 비밀번호
	 * @param name 사용자 이름
	 * @param role 사용자 역할
	 */
	public Users(String email, String password, String name, UserRole role) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.role = role;
	}

	/**
	 * 입찰 취소 패널티를 적용합니다.
	 * 현재 시점 시준으로 3일간 입찰등록이 제한됩니다.
	 */
	public void applyBidPenalty() {
		this.bidBlockedUntil = LocalDateTime.now().plusDays(3);
	}

	/**
	 * 현재 입찰등록이 제환된 상태인지 확인합니다.
	 * @return
	 */
	public boolean isBidBlocked() {
		return bidBlockedUntil != null && bidBlockedUntil.isAfter(LocalDateTime.now());
	}


}

