package com.sparta.cream.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.cream.dto.product.ProductSearchCondition;
import com.sparta.cream.entity.Product;

public interface ProductCustomRepository {
	Page<Product> searchProducts(ProductSearchCondition productSearchCondition, Pageable pageable);
}
