package com.sparta.cream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.cream.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	@Query(value = "SELECT * FROM product_image WHERE product_id IS NULL", nativeQuery = true)
	List<ProductImage> findOrphanedImages();
}
