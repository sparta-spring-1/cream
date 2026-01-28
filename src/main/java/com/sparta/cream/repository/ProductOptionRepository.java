package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductOption;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
	boolean existsByProductAndSize(Product oldProduct, String size);

	ProductOption findByProductAndSize(Product oldProduct, String size);
}
