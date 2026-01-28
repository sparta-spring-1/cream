package com.sparta.cream.dto.request;

import lombok.Getter;

@Getter
public class CreatePaymentRequest {
	private final Long tradeId;

	public CreatePaymentRequest(Long tradeId) {
		this.tradeId = tradeId;
	}
}
