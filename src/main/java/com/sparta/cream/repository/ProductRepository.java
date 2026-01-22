package com.sparta.cream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.entity.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface ProductRepository extends JpaRepository<Product, Long> {

	boolean existsByModelNumber(String modelNumber);
}
