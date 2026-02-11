package com.sparta.cream.dto.product;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSearchCondition {

	private ProductSortType sort;

	@Size(max = 50, message = "브랜드명은 50자를 초과할 수 없습니다.")
	private String brandName;

	private String category;

	private String productSize;

	@Min(value = 0, message = "최소 가격은 0 이상이어야 합니다.")
	private Integer minPrice;

	@Min(value = 0, message = "최대 가격은 0 이상이어야 합니다.")
	private Integer maxPrice;

	@Size(max = 100, message = "검색어는 100자를 초과할 수 없습니다.")
	private String keyword;

	@AssertTrue(message = "최소 가격은 최대 가격보다 클 수 없습니다.")
	public boolean isValidPriceRange() {
		if (minPrice == null || maxPrice == null) {
			return true;
		}
		return minPrice <= maxPrice;
	}
}
