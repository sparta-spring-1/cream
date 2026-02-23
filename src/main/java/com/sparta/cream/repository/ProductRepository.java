package com.sparta.cream.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.cream.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>,ProductCustomRepository {

	@Query("select p from Product p where p.id = :id and p.deletedAt is NULL")
	Optional<Product> findById(Long id);

	boolean existsByModelNumber(String modelNumber);

	@Query("""
    SELECT DISTINCT p
    FROM Product p
    LEFT JOIN FETCH p.productCategory pc
    INNER JOIN FETCH p.imageList pi
    WHERE p.id = :id
""")
	Optional<Product> findByIdWithGraph(Long id);

	@Query("""
    SELECT DISTINCT p
    FROM Product p
    LEFT JOIN FETCH p.productCategory pc
    LEFT JOIN FETCH p.imageList pi
    WHERE p.id = :productId
      AND p.deletedAt IS NULL
""")
	Optional<Product> findByIdAndDeletedAtIsNull(Long productId);
}
