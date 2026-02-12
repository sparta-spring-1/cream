package com.sparta.cream.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.dto.product.GetOneProductResponse;
import com.sparta.cream.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/{productId}")
	public ResponseEntity<GetOneProductResponse> getOneProduct(@PathVariable Long productId) {
		GetOneProductResponse response = productService.getPublicProduct(productId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
