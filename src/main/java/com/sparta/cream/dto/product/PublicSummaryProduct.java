package com.sparta.cream.dto.product;

import java.math.BigDecimal;

import com.sparta.cream.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicSummaryProduct {

	private Long productId;
	private String name;
	private String brandName;
	private BigDecimal retailPrice;

	public static PublicSummaryProduct from(Product product) {
		return new PublicSummaryProduct(
			product.getId(),
			product.getName(),
			product.getBrandName(),
			product.getRetailPrice()
		);
	}
}
