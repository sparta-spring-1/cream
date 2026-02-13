package com.sparta.cream.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	List<ProductImage> findByDeletedAtBefore(LocalDateTime localDateTime);
}
