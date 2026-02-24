package com.sparta.cream.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.cream.domain.bid.repository.BidRepository;
import com.sparta.cream.dto.product.AdminCreateProductResponse;
import com.sparta.cream.dto.product.AdminCreateProductRequest;
import com.sparta.cream.dto.product.AdminGetAllProductResponse;
import com.sparta.cream.dto.product.AdminGetOneProductResponse;
import com.sparta.cream.dto.product.AdminUpdateProductRequest;
import com.sparta.cream.dto.product.AdminUpdateProductResponse;
import com.sparta.cream.dto.product.GetAllProductResponse;
import com.sparta.cream.dto.product.GetOneProductResponse;
import com.sparta.cream.dto.product.ProductSearchCondition;
import com.sparta.cream.entity.BaseEntity;
import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductCategory;
import com.sparta.cream.entity.ProductImage;
import com.sparta.cream.entity.ProductOption;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ProductErrorCode;
import com.sparta.cream.repository.ProductCategoryRepository;
import com.sparta.cream.repository.ProductImageRepository;
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
	private final ProductImageRepository productImageRepository;
	private final BidRepository bidRepository;
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
			.retailDate(request.getRetailDate())
			.retailPrice(request.getRetailPrice())
			.sizeUnit(request.getSizeUnit())
			.color(request.getColor())
			.productCategory(category)
			.productStatus(request.getProductStatus())
			.operationStatus(request.getOperationStatus())
			.build();

		productRepository.save(product);

		if (!request.getImageIds().isEmpty()) {
			List<ProductImage> imageList = productImageRepository.findAllByIdIn(request.getImageIds());
			product.getImageList().addAll(imageList);
		}

		List<ProductOption> newOptions = new ArrayList<>();
		for (String size : request.getSizes()) {
			ProductOption productOption = ProductOption.builder()
				.product(product)
				.size(size)
				.build();

			newOptions.add(productOption);

		}
		productOptionRepository.saveAll(newOptions);

		return AdminCreateProductResponse.from(product);
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
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_CATEGORY));

		product.getImageList().stream()
			.filter(img -> !request.getImageIds().contains(img.getId()))
			.forEach(ProductImage::softDelete);

		// TODO 새로 추가된 이미지와 상품 연결
		// TODO 입찰 정보가 있는 상품은 수정할 수 없음

		// 상품 옵션 수정

		// 1. 기존 옵션 조회 (이미 영속성 컨텍스트에 관리됨)
		List<ProductOption> existingOptions = productOptionRepository.findAllByProduct(product);

		// 2. 요청된 사이즈들을 Set으로 변환 (비교 속도 향상 및 중복 방지)
		Set<String> requestedSizes = new HashSet<>(request.getOptions());
		Set<String> existingSizes = existingOptions.stream()
			.map(ProductOption::getSize)
			.collect(Collectors.toSet());

		// 3. 삭제 로직: 기존에 있었는데 요청에는 없는 사이즈 -> Soft Delete
		existingOptions.stream()
			.filter(option -> !requestedSizes.contains(option.getSize()))
			.forEach(ProductOption::softDelete);

		// 4. 추가 로직: 요청에는 있는데 기존에는 없었던 사이즈 -> New Entity
		List<ProductOption> newOptions = requestedSizes.stream()
			.filter(size -> !existingSizes.contains(size))
			.map(size -> ProductOption.builder()
				.product(product)
				.size(size)
				.build())
			.toList();

		productOptionRepository.saveAll(newOptions);

		// 5. 상품 기본 정보 업데이트
		product.update(request, category);

		// 응답용 사이즈 목록 (기존 유지 + 신규)
		List<String> finalSizes = Stream.concat(
			existingOptions.stream().filter(o -> requestedSizes.contains(o.getSize())).map(ProductOption::getSize),
			newOptions.stream().map(ProductOption::getSize)
		).toList();

		return AdminUpdateProductResponse.from(product, null, finalSizes);
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

		// 상품 조회
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		// 상품 옵션 삭제
		List<ProductOption> options = productOptionRepository.findAllByProduct(product);
		options.forEach(BaseEntity::softDelete);

		//TODO 해당 옵션들에 대한 입찰이 존재하면 삭제할 수 없음

		List<Long> imageIdList = product.getImageList().stream()
			.map(ProductImage::getId)
			.toList();

		product.getImageList().forEach(BaseEntity::softDelete);

		productImageRepository.deleteAllByIdInBatch(imageIdList);

		// 상품 삭제
		product.softDelete();
	}

	/**
	 * 관리자 권한으로 상품 목록을 조회합니다.
	 * 브랜드, 카테고리 등의 조건을 기반으로 상품을 검색하며 페이징 처리된 결과를 반환합니다.
	 *
	 * @param page 조회할 페이지 번호 (0부터 시작)
	 * @param pageSize 페이지당 조회할 상품 개수
	 * @param productSearchCondition 상품 검색 조건 모음
	 * @return 관리자 상품 목록 조회 응답 DTO
	 */
	public AdminGetAllProductResponse getAllProduct(int page, int pageSize, ProductSearchCondition productSearchCondition) {

		//TODO 정렬 조건

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());

		Page<Product> productPage =
			productRepository.searchProducts(
				productSearchCondition,
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
				condition,
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
	@Transactional(readOnly = true)
	public AdminGetOneProductResponse getOneProduct(Long productId) {
		//삭제된 상품을 포함하여 조회
		Product product = productRepository.findByIdWithGraph(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND_ID));

		List<String> options = productOptionRepository.findSizesByProductId(productId);
		List<Long> imageIds = product.getImageList().stream()
			.map(ProductImage::getId)
			.collect(Collectors.toList());

		return AdminGetOneProductResponse.from(product, options, imageIds);

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

		List<String> options = productOptionRepository.findSizesByProductId(productId);
		List<Long> imageIds = product.getImageList().stream()
			.map(ProductImage::getId)
			.collect(Collectors.toList());

		return GetOneProductResponse.from(product,options,imageIds);
	}

}
