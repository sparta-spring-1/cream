package com.sparta.cream.dto.response;

import lombok.Getter;

@Getter
public class CreatePaymentResponse {
	private final Long id;
	private final String paymentId;
	private final String status;
	private final String productName;
	private final Long amount;
	private final String email;
	private final String customerName;
	private final String customerPhoneNumber;

	public CreatePaymentResponse(Long id, String paymentId, String status, String productName, Long amount, String email, String customerName, String customerPhoneNumber) {
		this.id = id;
		this.paymentId = paymentId;
		this.status = status;
		this.productName = productName;
		this.amount = amount;
		this.email = email;
		this.customerName = customerName;
		this.customerPhoneNumber = customerPhoneNumber;
	}
}
