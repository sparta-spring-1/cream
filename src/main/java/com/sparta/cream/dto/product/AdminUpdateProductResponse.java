package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 상품 수정 결과를 반환하기 위한 응답 DTO 클래스입니다.
 * 관리자가 상품 수정 API를 호출한 후,
 * 수정된 상품의 최신 상태를 응답으로 전달하기 위해 사용됩니다.
 *
 * @author heoarim
 * @since 2026. 1. 27
 */
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
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public static AdminUpdateProductResponse from(Product product, List<Long> imageIds, List<String> options) {
		return new AdminUpdateProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getBrandName(),
			product.getProductCategory().getId(),
			imageIds,
			options,
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
