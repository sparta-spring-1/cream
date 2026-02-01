package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminGetOneProductResponse {

	private final Long id;
	private final String name;
	private final String modelNumber;
	private final String brandName;
	private final Long categoryId;
	private final List<Long> imageIds;
	private final List<String> options;
	private final String color;
	private final String sizeUnit;
	private final ProductStatus productStatus;
	private final OperationStatus operationStatus;
	private final BigDecimal retailPrice;
	private final LocalDateTime retailDate;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public static AdminGetOneProductResponse from(Product product) {
		return new AdminGetOneProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getBrandName(),
			product.getProductCategory().getId(),
			product.getImageIds(),
			product.getOptionSizes(),
			product.getColor(),
			product.getSizeUnit(),
			product.getProductStatus(),
			product.getOperationStatus(),
			product.getRetailPrice(),
			product.getRetailDate(),
			product.getCreatedAt(),
			product.getUpdatedAt()
		);
	}
}
