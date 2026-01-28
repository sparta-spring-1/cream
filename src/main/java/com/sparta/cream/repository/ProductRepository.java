package com.sparta.cream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.entity.Product;


public interface ProductRepository extends JpaRepository<Product, Long> {

	boolean existsByModelNumber(String modelNumber);

	List<Product> findByModelNumber(String modelNumber);
}
