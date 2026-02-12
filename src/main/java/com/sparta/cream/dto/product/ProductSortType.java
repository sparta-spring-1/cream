package com.sparta.cream.dto.product;

import org.springframework.data.domain.Sort;

public enum ProductSortType {

	RECENT("최신 등록순", Sort.by("createAt").descending()),
	PRICE_ASC("낮은 가격순", Sort.by("retailPrice").ascending()),;

	private final String description;
	private final Sort sort;

	ProductSortType(String description,Sort sort) {
		this.description = description;
		this.sort = sort;
	}

	public String getDescription() {
		return description;
	}

	public Sort getSort() {
		return sort;
	}
}

