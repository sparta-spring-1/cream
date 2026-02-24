package com.sparta.cream.dto.product;

import com.sparta.cream.entity.ProductOption;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductOptionInfo {
	private final Long id;
	private final String size;

	public static ProductOptionInfo from(ProductOption productOption) {
		return new ProductOptionInfo(
			productOption.getId(),
			productOption.getSize()
		);
	}
}
