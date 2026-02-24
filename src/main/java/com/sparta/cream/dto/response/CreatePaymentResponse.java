package com.sparta.cream.dto.response;

import java.math.BigDecimal;

import lombok.Getter;

/**
 * 결제 생성 후 반환되는 응답 데이터를 나타내는 DTO 클래스입니다.
 * <p>
 * id, (PortOne으로 송신할)paymentId, status, productName, amount, email, customerName, customerPhoneNumber를 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
public class CreatePaymentResponse {
	private final Long id;
	private final String paymentId;
	private final String status;
	private final String productName;
	private final BigDecimal amount;
	private final String email;
	private final String customerName;
	private final String customerPhoneNumber;

	public CreatePaymentResponse(Long id, String paymentId, String status, String productName, BigDecimal amount, String email, String customerName, String customerPhoneNumber) {
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
