package com.sparta.cream.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.cream.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	boolean existsByModelNumber(String modelNumber);

	List<Product> findByModelNumber(String modelNumber);

	@Query(
		value = """
            SELECT DISTINCT p
            FROM Product p
            LEFT JOIN p.productOptionList po
            WHERE (:brand IS NULL OR p.brandName = :brand)
              AND (:category IS NULL OR p.productCategory.id = :category)
              AND (:productSize IS NULL OR po.size = :productSize)
              AND (:minPrice IS NULL OR p.retailPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.retailPrice <= :maxPrice)
              AND (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
        """,
		countQuery = """
            SELECT COUNT(DISTINCT p)
            FROM Product p
            LEFT JOIN p.productOptionList po
            WHERE (:brand IS NULL OR p.brandName = :brand)
              AND (:category IS NULL OR p.productCategory.id = :category)
              AND (:productSize IS NULL OR po.size = :productSize)
              AND (:minPrice IS NULL OR p.retailPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.retailPrice <= :maxPrice)
              AND (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
        """
	)
	Page<Product> searchProducts(@Param("brand") String brand,
		@Param("category") Long category,
		@Param("productSize") String productSize,
		@Param("minPrice") Integer minPrice,
		@Param("maxPrice") Integer maxPrice,
		@Param("keyword") String keyword,
		Pageable pageable
	);

	@Query(
		value = "select * from product where id = :id",
		nativeQuery = true
	)
	Optional<Product> findByIdIncludingDeleted(Long id);
}
