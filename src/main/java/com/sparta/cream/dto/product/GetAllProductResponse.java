package com.sparta.cream.dto.product;

import java.util.List;

import org.springframework.data.domain.Page;

import com.sparta.cream.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품 목록 조회 API의 응답 DTO입니다.
 * 홈페이지 화면에서 상품 목록을 조회할 때 사용되며,
 * 상품 정보 리스트와 함께 페이징 처리를 위한 정보(hasNext, totalElements)를 제공합니다.
 *
 * @author heoarim
 * @since 2026. 2. 11
 */
@Getter
@AllArgsConstructor
public class GetAllProductResponse {
	private final List<PublicSummaryProduct> productList;
	private final Boolean hasNext;
	private final long totalElements;

	public static GetAllProductResponse from(Page<Product> pageProducts) {
		return new GetAllProductResponse(
			pageProducts.getContent().stream()
				.map(PublicSummaryProduct::from)
				.toList(),
			pageProducts.hasNext(),
			pageProducts.getTotalElements()
		);
	}
}
