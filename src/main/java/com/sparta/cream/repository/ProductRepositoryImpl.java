package com.sparta.cream.repository;

import static com.sparta.cream.entity.QProduct.*;
import static com.sparta.cream.entity.QProductCategory.*;
import static com.sparta.cream.entity.QProductOption.*;
import static org.springframework.util.StringUtils.hasText;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.cream.dto.product.ProductSearchCondition;
import com.sparta.cream.dto.product.ProductSortType;
import com.sparta.cream.entity.Product;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Product> searchProducts(ProductSearchCondition cond, Pageable pageable) {

		BooleanExpression[] predicates = {
			brandEq(cond.getBrandName()),
			categoryEq(cond.getCategory()),
			sizeEq(cond.getProductSize()),
			priceBetween(cond.getMinPrice(), cond.getMaxPrice()),
			nameContains(cond.getKeyword())
		};

		// 데이터 조회 쿼리
		List<Product> content = queryFactory
			.selectFrom(product)
			.leftJoin(product.productCategory, productCategory).fetchJoin()
			.leftJoin(productOption).on(productOption.product.eq(product))
			.where(predicates)
			.distinct()
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(getOrderSpecifier(cond.getSort()))
			.fetch();

		// 전체 개수 가져오기
		Long total = queryFactory
			.select(product.countDistinct()) // 중복을 제거한 상품의 개수만 카운트
			.from(product)
			.leftJoin(productOption).on(productOption.product.eq(product))
			.where(predicates)
			.fetchOne();

		//fetchOne()은 결과가 없으면 null을 반환할 수 있으므로
		long totalCount = (total != null) ? total : 0L;

		return new PageImpl<>(content, pageable, totalCount);
	}

	private BooleanExpression brandEq(String brandName) {
		return hasText(brandName) ? product.brandName.eq(brandName) : null;
	}

	private BooleanExpression categoryEq(String category) {
		// 카테고리 이름으로 검색하거나 ID로 검색하는 로직에 맞게 수정 가능
		return hasText(category) ? product.productCategory.name.eq(category) : null;
	}

	private BooleanExpression sizeEq(String size) {
		return hasText(size) ? productOption.size.eq(size) : null;
	}

	private BooleanExpression priceBetween(Integer min, Integer max) {
		if (min == null && max == null) return null;
		if (min == null) return product.retailPrice.loe(BigDecimal.valueOf(max));
		if (max == null) return product.retailPrice.goe(BigDecimal.valueOf(min));
		return product.retailPrice.between(BigDecimal.valueOf(min), BigDecimal.valueOf(max));
	}

	private BooleanExpression nameContains(String keyword) {
		return hasText(keyword) ? product.name.contains(keyword) : null;
	}

	private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
		if (sortType == null) {
			return product.id.desc();
		}

		return switch (sortType) {
			case RECENT -> product.createdAt.desc();
			case PRICE_ASC -> product.retailPrice.asc();
			default -> product.id.desc();
		};
	}
}
