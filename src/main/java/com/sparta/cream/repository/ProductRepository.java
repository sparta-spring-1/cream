package com.sparta.cream.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.cream.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("select p from Product p where p.id = :id and p.deletedAt is NULL")
	Optional<Product> findById(Long id);

	boolean existsByModelNumber(String modelNumber);

	@Query(
		value = """
            SELECT DISTINCT p
            FROM Product p
            WHERE (:brand IS NULL OR p.brandName = :brand)
              AND (:category IS NULL OR p.productCategory.id = :category)
              AND (:minPrice IS NULL OR p.retailPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.retailPrice <= :maxPrice)
              AND (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
        """,
		countQuery = """
            SELECT COUNT(DISTINCT p)
            FROM Product p
            WHERE (:brand IS NULL OR p.brandName = :brand)
              AND (:category IS NULL OR p.productCategory.id = :category)
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


	@Query("""
    SELECT DISTINCT p
    FROM Product p
    LEFT JOIN FETCH p.productCategory pc
    INNER JOIN FETCH p.imageList pi
    WHERE p.id = :id
""")
	Optional<Product> findByIdWithGraph(Long id);
}
