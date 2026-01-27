package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductImage;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUpdateProductResponse {

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
	private final LocalDateTime createAt;
	private final LocalDateTime updateAt;

	public static AdminUpdateProductResponse from(Product product) {
		return new AdminUpdateProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getBrandName(),
			product.getProductCategory().getId(),
			getImageIds(product),
			getOptions(product),
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

	private static List<Long> getImageIds(Product product) {
		if(product.getImageList() == null || product.getImageList().isEmpty()) {
			return Collections.emptyList();
		}
		return product.getImageList().stream()
			.map(ProductImage::getId)
			.toList();
	}

	private static List<String> getOptions(Product product) {
		if(product.getProductOptionList() == null || product.getProductOptionList().isEmpty()) {
			return Collections.emptyList();
		}
		return product.getProductOptionList().stream()
			.map(ProductOption::getSize)
			.toList();
	}
}
