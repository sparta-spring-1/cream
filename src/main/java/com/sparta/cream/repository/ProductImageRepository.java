package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

}
