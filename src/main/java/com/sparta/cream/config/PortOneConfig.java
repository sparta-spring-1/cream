package com.sparta.cream.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.portone.sdk.server.PortOneClient;
import lombok.Getter;

/**
 * PortOne API 관련 설정을 담당하는 Configuration 클래스입니다.
 * <p>
 * application.yml에서 PortOne API의 기본 URL, Secret Key, Store ID, Channel Key를 주입받아 사용합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Configuration
@Getter
public class PortOneConfig {

    private final String baseUrl;
    private final String apiSecret;
	private final String storeId;
	private final String channelKey;

    public PortOneConfig(@Value("${portone.api.base-url}") String baseUrl,
		@Value("${portone.api.secret}") String apiSecret,
		@Value("${portone.api.store-id}") String storeId,
		@Value("${portone.api.channel-key}") String channelKey) {
        this.baseUrl = baseUrl;
        this.apiSecret = apiSecret;
		this.storeId = storeId;
		this.channelKey = channelKey;
    }

	@Bean
	public PortOneClient portOneClient() {
		return new PortOneClient(apiSecret, baseUrl, storeId);
	}
}
