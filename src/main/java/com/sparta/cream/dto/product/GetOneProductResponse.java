package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품 단건 조회 API의 응답 DTO입니다.
 * 사용자에게 보여질 상품의 최소 정보를 포함합니다.
 *
 * @author heoarim
 * @since 2026. 2. 11
 */
@Getter
@AllArgsConstructor
public class GetOneProductResponse {
	private final Long id;
	private final String name;
	private final String modelNumber;
	private final String brandName;
	private final Long categoryId;
	private final List<Long> imageIds;
	private final List<String> options;
	private final String color;
	private final String sizeUnit;
	private final BigDecimal retailPrice;
	private final LocalDateTime retailDate;

	public static GetOneProductResponse from(Product product, List<String> options, List<Long> imageIds) {
		return new GetOneProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getBrandName(),
			product.getProductCategory().getId(),
			imageIds,
			options,
			product.getColor(),
			product.getSizeUnit(),
			product.getRetailPrice(),
			product.getRetailDate()
		);
	}
}
