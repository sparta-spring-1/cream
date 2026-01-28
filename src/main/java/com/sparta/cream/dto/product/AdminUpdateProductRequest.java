package com.sparta.cream.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.cream.entity.OperationStatus;
import com.sparta.cream.entity.ProductStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 관리자 상품 수정 요청을 위한 DTO 클래스.
 *
 * <p>
 * 관리자가 기존 상품 정보를 수정할 때 사용하는 요청 객체로 상품의 정보를 포함합니다.
 * 각 필드는 Bean Validation을 통해 입력값 검증이 수행됩니다.
 * 유효하지 않은 요청은 컨트롤러 단에서 예외로 처리됩니다.
 * </p>
 *
 * @author heoarim
 * @since 2026. 1. 27
 */
@Getter
public class AdminUpdateProductRequest {

	@NotBlank(message = "상품명은 필수입니다.")
	@Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
	@Pattern(
		regexp = "^[a-zA-Z0-9가-힣\\s()\\-]+$",
		message = "상품명에 허용되지 않은 문자가 포함되어 있습니다."
	)
	private String name;

	@NotBlank(message = "모델번호는 필수입니다.")
	@Size(max = 50, message = "모델번호는 50자를 초과할 수 없습니다.")
	private String modelNumber;

	@NotBlank
	@Size(max = 50, message = "브랜드명은 50자를 초과할 수 없습니다.")
	private String brandName;

	@NotNull(message = "카테고리는 필수 값입니다.")
	private Long categoryId;

	@NotNull(message = "이미지는 필수 값입니다.")
	private List<Long> imageIds;

	@NotNull(message = "사이즈는 필수 값입니다.")
	private List<String> options;

	@Size(max = 30)
	private String color;

	@Size(max = 30)
	private String sizeUnit;

	@NotNull
	private ProductStatus productStatus;

	@NotNull
	private OperationStatus operationStatus;

	@NotNull(message = "발매가는 필수 값입니다.")
	@Positive
	private BigDecimal retailPrice;

	private LocalDateTime retailDate;
}
