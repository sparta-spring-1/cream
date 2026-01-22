package com.sparta.cream.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.dto.product.AdminCreateProductResponse;
import com.sparta.cream.dto.product.AdminCreateProductRequest;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductCategory;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ProductErrorCode;
import com.sparta.cream.repository.ProductCategoryRepository;
import com.sparta.cream.repository.ProductOptionRepository;
import com.sparta.cream.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final ProductOptionRepository productOptionRepository;
	@Transactional
	public AdminCreateProductResponse createProduct(AdminCreateProductRequest request) {

		if (productRepository.existsByModelNumber(request.getModelNumber()))
		{
			throw new BusinessException(ProductErrorCode.PRODUCT_MODELNUMBER_CONFLICT);
		}

		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
			.orElseThrow(() ->
				new BusinessException(ProductErrorCode.PRODUCT_NOTFOUND_CATEGORY,"존재하지 않는 카테고리입니다.")
			);

		Product product = Product.builder()
			.name(request.getName())
			.brandName(request.getBrandName())
			.modelNumber(request.getModelNumber())
			.retailDate(request.getRetailDate())
			.retailPrice(request.getRetailPrice())
			.sizeUnit(request.getSizeUnit())
			.color(request.getColor())
			.productCategory(category)
			.productStatus(request.getProductStatus())
			.operationStatus(request.getOperationStatus())
			.build();

		Product savedProduct = productRepository.save(product);

		for(String size : request.getSizes()){
			ProductOption productOption = ProductOption.builder()
				.product(savedProduct)
				.size(size)
				.build();

			productOptionRepository.save(productOption);
		}

		return AdminCreateProductResponse.from(savedProduct);
	}
}
