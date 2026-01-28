package com.sparta.cream.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.sparta.cream.dto.product.AdminUpdateProductRequest;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ProductErrorCode;

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
import lombok.Builder;
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
@SQLRestriction("is_deleted = false")
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

	@Builder
	public Product(String name, String modelNumber, String brandName, ProductCategory productCategory,
		List<ProductImage> imageList, List<ProductOption> productOptionList, String color, String sizeUnit,
		ProductStatus productStatus, OperationStatus operationStatus, BigDecimal retailPrice,
		LocalDateTime retailDate) {
		this.name = name;
		this.modelNumber = modelNumber;
		this.brandName = brandName;
		this.productCategory = productCategory;
		this.imageList = imageList;
		this.productOptionList = productOptionList;
		this.color = color;
		this.sizeUnit = sizeUnit;
		this.productStatus = productStatus;
		this.operationStatus = operationStatus;
		this.retailPrice = retailPrice;
		this.retailDate = retailDate;
	}

	public void update(AdminUpdateProductRequest request, ProductCategory productCategory,
		List<ProductImage> imageList, List<ProductOption> productOptionList
	) {
		this.name = request.getName();
		this.modelNumber = request.getModelNumber();
		this.brandName = request.getBrandName();
		this.productCategory = productCategory;
		this.imageList = imageList;
		this.productOptionList = productOptionList;
		this.color = request.getColor();
		this.sizeUnit = request.getSizeUnit();
		this.productStatus = request.getProductStatus();
		this.operationStatus = request.getOperationStatus();
		this.retailPrice = request.getRetailPrice();
		this.retailDate = request.getRetailDate();
	}

	public void createOption(List<ProductOption> productOption) {
		this.productOptionList = productOption;
	}

	public void softDelete() {
		if(this.productStatus == ProductStatus.ON_SALE){
			throw new BusinessException(ProductErrorCode.PRODUCT_CANNOT_DELETE_ON_SALE);
		}
		super.softDelete();
		imageList.forEach(BaseEntity::softDelete);
		productOptionList.forEach(BaseEntity::softDelete);
	}
}

