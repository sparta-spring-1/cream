package com.sparta.cream.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Column(nullable = false, length = 20, unique = true)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	/**
	 * 사용자 생성자
	 * 기본 역할은 USER로 설정됩니다.
	 *
	 * @param email 사용자 이메일
	 * @param password 암호화된 비밀번호
	 * @param name 사용자 이름
	 * @param phoneNumber 사용자 전화번호
	 */
	public Users(String email, String password, String name, String phoneNumber) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.phoneNumber = phoneNumber;
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
	public Users(String email, String password, String name, String phoneNumber, UserRole role) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.role = role;
	}
}

