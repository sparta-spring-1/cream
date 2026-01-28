package com.sparta.cream.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.dto.product.AdminCreateProductResponse;
import com.sparta.cream.dto.product.AdminCreateProductRequest;
import com.sparta.cream.dto.product.AdminUpdateProductRequest;
import com.sparta.cream.dto.product.AdminUpdateProductResponse;
import com.sparta.cream.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

	private final ProductService productService;

	@PostMapping
	public ResponseEntity<AdminCreateProductResponse> createProduct(
		@RequestBody @Valid AdminCreateProductRequest request
	) {
		AdminCreateProductResponse response = productService.createProduct(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{productId}")
	public ResponseEntity<AdminUpdateProductResponse> updateProduct(
		@PathVariable Long productId,
		@RequestBody @Valid AdminUpdateProductRequest request
	) {
		AdminUpdateProductResponse response = productService.updateProduct(productId,request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}

