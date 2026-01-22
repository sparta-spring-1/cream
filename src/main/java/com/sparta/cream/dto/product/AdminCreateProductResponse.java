package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminCreateProductResponse {

	private Long id;
	private String name;
	private String modelNumber;
	private String brandName;
	private Long categoryId;
	private List<Long> imageIds;
	private List<Long> productIds;
	private String color;
	private String sizeUnit;
	private ProductStatus productStatus;
	private OperationStatus operationStatus;
	private BigDecimal retailPrice;
	private LocalDateTime retailDate;
	private LocalDateTime createDate;

	public static AdminCreateProductResponse from(Product savedProduct) {
	}
}
