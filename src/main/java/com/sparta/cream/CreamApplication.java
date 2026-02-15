package com.sparta.cream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.sparta.cream.domain.trade.service.TradeService;

@EnableAsync
@EnableRetry
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class CreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreamApplication.class, args);
	}

	/**
	 * 애플리케이션 구동 직후 미체결 입찰 매칭을 수행하는 Runner
	 * 서버가 재시작될 때 쌓여있던 PENDING 상태의 입찰들을 즉시 체결 엔진으로 전달합니다.
	 *
	 * @param tradeService 체결 로직을 수행할 서비스 빈
	 * @return CommandLineRunner 실행 객체
	 */
	@Bean
	public CommandLineRunner run(TradeService tradeService) {
		return args -> {
			java.util.concurrent.CompletableFuture.runAsync(() -> {
				try {
					System.out.println("체결 엔진 가동: 백그라운드에서 미체결 입찰 매칭 시작...");
					tradeService.matchAllPendingBids();
					System.out.println("체결 엔진 가동 완료.");
				} catch (Exception e) {
					System.err.println("부팅 시 체결 매칭 중 오류 발생: " + e.getMessage());
				}
			});
		};
	}
}
