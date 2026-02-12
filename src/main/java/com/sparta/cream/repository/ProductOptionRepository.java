package com.sparta.cream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.cream.entity.Product;
import com.sparta.cream.entity.ProductOption;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

	@Query("""
    select po.size
    from ProductOption po
    where po.product.id = :productId
""")
	List<String> findSizesByProductId(Long productId);

	List<ProductOption> findAllByProduct(Product product);
}
