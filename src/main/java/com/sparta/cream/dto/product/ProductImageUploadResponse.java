package com.sparta.cream.dto.product;

import com.sparta.cream.entity.ProductImage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductImageUploadResponse {
	private final Long imageId;
	private final String originalFileName;
	private final String objectKey;
	private final String url;

	public static ProductImageUploadResponse from(ProductImage productImage) {
		return new ProductImageUploadResponse(
			productImage.getId(),
			productImage.getFileName(),
			productImage.getObjectKey(),
			productImage.getUrl()
		);
	}
}
