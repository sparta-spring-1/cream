package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminGetAllProductResponse {
	private final List<ProductInfo> productList;
	private final Boolean hasNext;
	private final int totalElements;

	public static AdminGetAllProductResponse from(List<Product> products, Boolean hasNext, int totalElements) {
		return new AdminGetAllProductResponse(
			products.stream()
				.map(ProductInfo::from)
				.toList(),
			hasNext,
			totalElements
		);
	}

	@Getter
	@AllArgsConstructor
	public static class ProductInfo {

		private Long productId;
		private String name;
		private String modelNumber;
		private Long categoryId;
		private BigDecimal retail_price;
		private ProductStatus productStatus;
		private OperationStatus operationStatus;

		public static ProductInfo from(Product product) {
			return new ProductInfo(product.getId(),
				product.getName(),
				product.getModelNumber(),
				product.getProductCategory().getId(),
				product.getRetailPrice(),
				product.getProductStatus(),
				product.getOperationStatus()
			);
		}
	}
}

