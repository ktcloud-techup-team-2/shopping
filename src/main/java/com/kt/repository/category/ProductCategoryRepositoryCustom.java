package com.kt.repository.category;

import java.util.Collection;
import java.util.List;

import com.kt.domain.category.ProductCategory;

public interface ProductCategoryRepositoryCustom {
	List<ProductCategory> findAllWithCategoryByProductId(Long productId);
	List<ProductCategory> findAllWithCategoryByProductIdIn(Collection<Long> productIds);
}
