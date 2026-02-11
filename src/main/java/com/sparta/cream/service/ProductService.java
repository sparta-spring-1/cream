package com.sparta.cream.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.dto.product.AdminCreateProductResponse;
import com.sparta.cream.dto.product.AdminCreateProductRequest;
import com.sparta.cream.dto.product.AdminGetAllProductResponse;
import com.sparta.cream.dto.product.AdminGetOneProductResponse;
import com.sparta.cream.dto.product.AdminUpdateProductRequest;
import com.sparta.cream.dto.product.AdminUpdateProductResponse;
import com.sparta.cream.dto.product.GetAllProductResponse;
import com.sparta.cream.dto.product.GetOneProductResponse;
import com.sparta.cream.dto.product.ProductSearchCondition;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductCategory;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ProductErrorCode;
import com.sparta.cream.repository.ProductCategoryRepository;
import com.sparta.cream.repository.ProductOptionRepository;
import com.sparta.cream.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 상품 도메인의 비즈니스 로직을 담당하는 서비스 클래스.
 *
 * <p>
 * 상품 생성 및 수정에 대한 핵심 유스케이스를 처리하며,
 * 상품(Product), 카테고리(ProductCategory), 옵션(ProductOption) 간의
 * 연관관계를 일관성 있게 관리한다.
 * </p>
 *
 * <p>
 * 본 서비스는 다음 책임을 가진다.
 * <ul>
 *   <li>상품 생성 시 중복 모델 번호 검증</li>
 *   <li>카테고리 존재 여부 검증</li>
 *   <li>상품 옵션의 일괄 생성 및 수정</li>
 *   <li>트랜잭션 경계 내에서 도메인 상태 변경 보장</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final ProductOptionRepository productOptionRepository;

	/**
	 * 관리자 상품을 신규로 생성한다.
	 *
	 * <p>
	 * 상품 생성 시 엔티티 생성 및 저장하고
	 * 상품과 옵션 간 연관관계를 설정한다.
	 * </p>
	 *
	 * @param request 관리자 상품 생성 요청 DTO
	 * @return 생성된 상품 정보를 담은 응답 DTO
	 * @throws BusinessException 모델 번호가 이미 존재하거나,
	 *                           카테고리가 존재하지 않는 경우
	 */
	@Transactional
	public AdminCreateProductResponse createProduct(AdminCreateProductRequest request) {

		if (productRepository.existsByModelNumber(request.getModelNumber())) {
			throw new BusinessException(ProductErrorCode.PRODUCT_MODELNUMBER_CONFLICT);
		}

		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
			.orElseThrow(() ->
				new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_CATEGORY, "존재하지 않는 카테고리입니다.")
			);

		Product product = Product.builder()
			.name(request.getName())
			.brandName(request.getBrandName())
			.modelNumber(request.getModelNumber())
			.imageList(null)
			.productOptionList(null)
			.retailDate(request.getRetailDate())
			.retailPrice(request.getRetailPrice())
			.sizeUnit(request.getSizeUnit())
			.color(request.getColor())
			.productCategory(category)
			.productStatus(request.getProductStatus())
			.operationStatus(request.getOperationStatus())
			.build();

		Product savedProduct = productRepository.save(product);

		List<ProductOption> newOptions = new ArrayList<>();
		for (String size : request.getSizes()) {
			ProductOption productOption = ProductOption.builder()
				.product(savedProduct)
				.size(size)
				.build();

			newOptions.add(productOption);

		}
		productOptionRepository.saveAll(newOptions);
		product.createOption(newOptions);

		return AdminCreateProductResponse.from(savedProduct);
	}

	/**
	 * 관리자 상품 정보를 수정한다.
	 *
	 * <p>
	 * 상품 수정 시 다음 사항을 고려한다.
	 *  - 옵션 수정 로직은 "존재 여부 판단 → 신규 생성 또는 기존 재사용" 방식으로 처리하여 불필요한 옵션 중복 생성을 방지한다.
	 * </p>
	 *
	 * @param productId 수정 대상 상품 ID
	 * @param request 관리자 상품 수정 요청 DTO
	 * @return 수정된 상품 정보를 담은 응답 DTO
	 * @throws BusinessException 상품 또는 카테고리가 존재하지 않는 경우
	 */
	@Transactional
	public AdminUpdateProductResponse updateProduct(Long productId, AdminUpdateProductRequest request) {
		Product oldProduct = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_CATEGORY));

		// TODO 상품 이미지 수정

		// 상품 옵션 수정
		List<ProductOption> newOptions = new ArrayList<>();
		for (String size : request.getOptions()) {
			if (!productOptionRepository.existsByProductAndSize(oldProduct, size)) {
				ProductOption productOption = ProductOption.builder()
					.product(oldProduct)
					.size(size)
					.build();

				newOptions.add(productOption);
			} else {
				ProductOption oldOption = productOptionRepository.findByProductAndSize(oldProduct, size);
				newOptions.add(oldOption);
			}
		}
		List<ProductOption> savedOptions = productOptionRepository.saveAll(newOptions);

		oldProduct.update(request, category, null, savedOptions);

		Product newProduct = productRepository.save(oldProduct);

		return AdminUpdateProductResponse.from(newProduct);
	}

	/**
	 * 상품을 소프트 삭제 처리한다.
	 * 전달받은 상품 ID로 상품을 조회한 뒤,
	 * 실제 레코드를 삭제하지 않고 삭제 플래그를 변경하는 방식으로 삭제한다.
	 *
	 * @param productId 삭제할 상품의 ID
	 * @throws BusinessException 상품이 존재하지 않는 경우
	 */
	@Transactional
	public void deleteProduct(Long productId) {

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		// 상품 삭제
		product.softDelete();
	}

	/**
	 * 관리자 권한으로 상품 목록을 조회합니다.
	 * 브랜드, 카테고리 등의 조건을 기반으로 상품을 검색하며 페이징 처리된 결과를 반환합니다.
	 *
	 * @param page 조회할 페이지 번호 (0부터 시작)
	 * @param pageSize 페이지당 조회할 상품 개수
	 * @param sort 정렬 조건
	 * @param brand 브랜드 필터 조건
	 * @param category 카테고리 ID 필터 조건
	 * @param productSize 상품 사이즈 필터 조건
	 * @param minPrice 최소 가격 필터 조건
	 * @param maxPrice 최대 가격 필터 조건
	 * @param keyword 상품명 검색 키워드
	 * @return 관리자 상품 목록 조회 응답 DTO
	 */
	public AdminGetAllProductResponse getAllProduct(int page, int pageSize, String sort, String brand, Long category, String productSize, Integer minPrice, Integer maxPrice, String keyword) {

		//TODO 정렬 조건

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());

		Page<Product> productPage =
			productRepository.searchProducts(
				brand,
				category,
				productSize,
				minPrice,
				maxPrice,
				keyword,
				pageable
			);

		return AdminGetAllProductResponse.from(productPage);
	}

	/**
	 * 상품 목록을 조회합니다.
	 * 브랜드, 카테고리 등의 조건을 기반으로 상품을 검색하며 페이징 처리된 결과를 반환합니다.
	 *
	 * @param page 조회할 페이지 번호 (0부터 시작)
	 * @param pageSize 페이지당 조회할 상품 개수
	 * ProductSearchCondition 다중 필터 조건 dto
	 * @return 상품 목록 조회 응답 DTO
	 */
	public GetAllProductResponse getAllPublicProduct(int page, int pageSize, ProductSearchCondition condition) {

		Sort sort = Sort.by("id").descending();
		if(condition.getSort()!=null) {
			sort = condition.getSort().getSort();
		}

		Pageable pageable = PageRequest.of(page, pageSize, sort);

		Long categoryId = null;

		if(condition.getCategory() != null) {
			ProductCategory productCategory = productCategoryRepository.findByName(condition.getCategory())
											.orElseThrow(()-> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_CATEGORY));
			categoryId = productCategory.getId();
		}

		Page<Product> productPage =
			productRepository.searchProducts(
				condition.getBrandName(),
				categoryId,
				condition.getProductSize(),
				condition.getMinPrice(),
				condition.getMaxPrice(),
				condition.getKeyword(),
				pageable
			);

		return GetAllProductResponse.from(productPage);
	}

	/**
	 * 관리자 권한으로 상품 단건을 조회합니다.
	 * 일반 사용자 조회와 달리 Soft Delete 처리된 상품도 함께 조회합니다.
	 *
	 * @param productId 조회할 상품의 ID
	 * @return 삭제 여부와 관계없이 조회된 상품 정보를 담은 응답 DTO
	 * @throws BusinessException 상품이 존재하지 않을 경우 발생
	 */
	public AdminGetOneProductResponse getOneProduct(Long productId) {
		//삭제된 상품을 포함하여 조회
		Product product = productRepository.findByIdIncludingDeleted(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		return AdminGetOneProductResponse.from(product);
	}

	/**
	 * 상품 단건을 조회합니다.
	 * Soft Delete 처리된 상품 조회시 예외처리합니다.
	 *
	 * @param productId 조회할 상품의 ID
	 * @return 조회된 상품 정보를 담은 응답 DTO
	 * @throws BusinessException 상품이 존재하지 않을 경우 발생
	 */
	public GetOneProductResponse getPublicProduct(Long productId) {

		//삭제된 상품을 제외하고 조회
		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		return GetOneProductResponse.from(product);
	}

}
