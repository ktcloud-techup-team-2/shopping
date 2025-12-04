package com.kt.repository.category;

import java.util.Collection;

import com.kt.domain.category.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, ProductCategoryRepositoryCustom {
	void deleteByProductId(Long productId);
	long countByCategoryIdIn(Collection<Long> categoryIds);
}