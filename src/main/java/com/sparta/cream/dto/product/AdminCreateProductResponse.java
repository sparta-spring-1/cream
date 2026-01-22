package com.sparta.cream.dto.product;

import java.time.LocalDateTime;

import com.sparta.cream.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminCreateProductResponse {

	private Long id;
	private String name;
	private String modelNumber;
	private LocalDateTime createDate;

	public static AdminCreateProductResponse from(Product product) {
		return new AdminCreateProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getCreatedAt()
		);
	}
}
