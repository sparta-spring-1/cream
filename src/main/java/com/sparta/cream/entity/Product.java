package com.sparta.cream.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"model_number", "brand_name"})
	}
)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 50)
	private String modelNumber;

	@Column(nullable = false, length = 50)
	private String brandName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private ProductCategory productCategory;

	@OneToMany(mappedBy = "product")
	private List<ProductImage> imageList = new ArrayList<>();

	@OneToMany(mappedBy = "product")
	private List<ProductOption> productOptionList = new ArrayList<>();

	@Column(length = 30)
	private String color;

	@Column(length = 30)
	private String sizeUnit;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ProductStatus productStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OperationStatus operationStatus;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal retailPrice;

	private LocalDateTime retailDate;

}

