package com.sparta.cream.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PortOneConfig {

    private final String baseUrl;
    private final String apiSecret;

    public PortOneConfig(@Value("${portone.api.base-url}") String baseUrl,
		@Value("${portone.api.secret}") String apiSecret) {
        this.baseUrl = baseUrl;
        this.apiSecret = apiSecret;
    }

    @Bean
    public RestClient portOneRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(60));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }
}
