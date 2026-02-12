package com.sparta.cream.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.sparta.cream.config.PortOneConfig;
import com.sparta.cream.dto.portone.PortOnePaymentResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortOneApiClient {

	private final PortOneConfig portOneConfig;
	private final RestTemplate restTemplate;

	public PortOnePaymentResponse getPayment(String merchantUid) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "PortOne " + portOneConfig.getApiSecret());
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String url = portOneConfig.getBaseUrl() + "/payments/" + merchantUid + "?storeId="
			+ portOneConfig.getStoreId();

		ResponseEntity<PortOnePaymentResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity,
			PortOnePaymentResponse.class);

		return response.getBody();
	}

	public void cancelPayment(String merchantUid, BigDecimal totalAmount, String reason, BigDecimal cancellableAmount,
		String refundEmail) {

		int amount = totalAmount.intValue();
		int currentCancellableAmount = cancellableAmount.intValue();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "PortOne " + portOneConfig.getApiSecret());
		headers.set("Content-Type", "application/json");

		String url = portOneConfig.getBaseUrl() + "/payments/" + merchantUid + "/cancel";

		Map<String, Object> requestBody = Map.of(
			"storeId", portOneConfig.getStoreId(),
			"amount", amount,
			"reason", reason,
			"currentCancellableAmount", currentCancellableAmount,
			"refundEmail", refundEmail
		);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		restTemplate.postForEntity(url, requestEntity, Map.class);
	}
}
