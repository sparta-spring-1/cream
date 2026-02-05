package com.sparta.cream.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정 클래스입니다.
 * JPAQueryFactory를 Spring Bean으로 등록하여
 * 레포지토리 또는 Custom 레포지토리 에서
 * QueryDSL 기반의 쿼리를 작성할 수 있도록 합니다.
 * QueryDslConfig.java
 *
 * @author kimsehyun
 * @since 2026. 1. 27.
 */
@Configuration
public class QueryDslConfig {

	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}
}
