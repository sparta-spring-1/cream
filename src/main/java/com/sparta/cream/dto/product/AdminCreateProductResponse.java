package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductStatus;

import jakarta.validation.constraints.NotBlank;
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
