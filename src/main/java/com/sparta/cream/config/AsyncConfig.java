package com.sparta.cream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 애플리케이션의 비동기 실행 설정을 담당하는 설정 클래스입니다.
 * 특히 TradeService의 매칭 로직이 사용자 응답 스레드와 분리되어
 * 별도의 스레드 풀에서 안전하게 실행되도록 관리합니다.
 * AsyncConfig.java
 *
 * @author kimsehyun
 * @since 2026. 02. 10.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * 비동기 작업 처리를 위한 ThreadPoolTaskExecutor를 생성합니다.
	 * 입찰 및 체결 매칭 엔진이 고부하 상황에서도 안정적으로 동작하도록 자원을 할당합니다.
	 *
	 * @return 설정된 TaskExecutor 인스턴스
	 */
	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(50);
		executor.setMaxPoolSize(100);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("Async-");
		executor.initialize();
		return executor;
	}
}
