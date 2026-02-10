package com.sparta.cream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	@Query("""
    select po.id
    from ProductImage po
    where po.product.id = :productId
	""")
	List<Long> findIdsByProductId(Long productId);

	List<ProductImage> findAllByProduct(Product product);
}
