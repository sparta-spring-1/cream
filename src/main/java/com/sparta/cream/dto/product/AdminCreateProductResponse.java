package com.sparta.cream.dto.product;

import java.time.LocalDateTime;

import com.sparta.cream.entity.Product;

import lombok.Getter;

@Getter
public class AdminCreateProductResponse {

	private final Long id;
	private final String name;
	private final String modelNumber;
	private final LocalDateTime createDate;

	public AdminCreateProductResponse(Long id, String name, String modelNumber, LocalDateTime createDate) {
		this.id = id;
		this.name = name;
		this.modelNumber = modelNumber;
		this.createDate = createDate;
	}

	public static AdminCreateProductResponse from(Product product) {
		return new AdminCreateProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getCreatedAt()
		);
	}
}
