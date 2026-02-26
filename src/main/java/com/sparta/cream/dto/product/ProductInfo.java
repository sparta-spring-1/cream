package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInfo {

	private Long productId;
	private String name;
	private String modelNumber;
	private Long categoryId;
	private BigDecimal retailPrice;
	private ProductStatus productStatus;
	private OperationStatus operationStatus;
	private LocalDateTime deletedAt;

	public static ProductInfo from(Product product) {
		return new ProductInfo(product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getProductCategory().getId(),
			product.getRetailPrice(),
			product.getProductStatus(),
			product.getOperationStatus(),
			product.getDeletedAt()
		);
	}
}
