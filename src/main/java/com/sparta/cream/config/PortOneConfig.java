package com.sparta.cream.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.portone.sdk.server.PortOneClient;
import lombok.Getter;

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
