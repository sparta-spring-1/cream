package com.sparta.cream.service;

import org.springframework.stereotype.Service;

import com.sparta.cream.dto.product.AdminCreateProductResponse;
import com.sparta.cream.dto.product.AdminProductCreateRequest;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductCategory;
import com.sparta.cream.repository.ProductCategoryRepository;
import com.sparta.cream.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;

	public AdminCreateProductResponse createProduct(AdminProductCreateRequest request) {

		if (productRepository.existsByModelNumber(request.getModelNumber()))
		{
			throw new IllegalStateException("이미 등록된 상품입니다.");
		}

		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
			.orElseThrow(() ->
				new IllegalStateException("존재하지 않는 카테고리입니다.")
			);

		Product product = Product.builder()
			.name(request.getName())
			.brandName(request.getBrandName())
			.retailDate(request.getRetailDate())
			.retailPrice(request.getRetailPrice())
			.sizeUnit(request.getSizeUnit())
			.color(request.getColor())
			.productCategory(category)
			.build();

		Product savedProduct = productRepository.save(product);

		return AdminCreateProductResponse.from(savedProduct);
	}
}
