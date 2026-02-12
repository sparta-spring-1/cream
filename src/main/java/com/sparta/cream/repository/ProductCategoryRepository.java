package com.sparta.cream.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.entity.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
	Optional<ProductCategory> findByName(String category);
}
