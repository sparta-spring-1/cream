package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 상품 단건 조회 API의 응답 DTO입니다.
 * 상품의 기본 정보뿐만 아니라 상태 정보,가격 등 관리자가 확인해야 하는 모든 정보를 포함합니다.
 *
 * @author heoarim
 * @since 2026. 2. 2
 */
@Getter
@AllArgsConstructor
public class AdminGetOneProductResponse {

	private final Long id;
	private final String name;
	private final String modelNumber;
	private final String brandName;
	private final Long categoryId;
	private final List<String> imageUrls;
	private final List<ProductOptionInfo> options;
	private final String color;
	private final String sizeUnit;
	private final ProductStatus productStatus;
	private final OperationStatus operationStatus;
	private final BigDecimal retailPrice;
	private final LocalDateTime retailDate;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public static AdminGetOneProductResponse from(Product product, List<ProductOptionInfo> options, List<String> imageUrls) {
		return new AdminGetOneProductResponse(
			product.getId(),
			product.getName(),
			product.getModelNumber(),
			product.getBrandName(),
			product.getProductCategory().getId(),
			imageUrls,
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
