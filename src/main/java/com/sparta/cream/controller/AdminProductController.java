package com.sparta.cream.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.dto.product.AdminCreateProductResponse;
import com.sparta.cream.dto.product.AdminCreateProductRequest;
import com.sparta.cream.dto.product.AdminGetAllProductResponse;
import com.sparta.cream.dto.product.AdminGetOneProductResponse;
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
		AdminUpdateProductResponse response = productService.updateProduct(productId, request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> deleteProduct(
		@PathVariable Long productId
	) {
		productService.deleteProduct(productId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping
	public ResponseEntity<AdminGetAllProductResponse> getAllProduct(
		@RequestParam(required = true) int page,
		@RequestParam(required = true) int pageSize,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) String brand,
		@RequestParam(required = false) Long category,
		@RequestParam(required = false) String productSize,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) Integer maxPrice,
		@RequestParam(required = false) String keyword
	) {
		AdminGetAllProductResponse response =
			productService.getAllProduct(page, pageSize, sort, brand, category, productSize, minPrice, maxPrice,
				keyword);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<AdminGetOneProductResponse> getOneProduct(@PathVariable Long productId) {
		AdminGetOneProductResponse response = productService.getOneProduct(productId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}

