package com.kt.repository.category;

import com.kt.domain.category.ProductCategory;
import com.kt.domain.category.QCategory;
import com.kt.domain.category.QProductCategory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ProductCategory> findAllWithCategoryByProductId(Long productId) {
		QProductCategory productCategory = QProductCategory.productCategory;
		QCategory category = QCategory.category;

		return queryFactory.selectFrom(productCategory)
			.join(productCategory.category, category).fetchJoin()
			.where(productCategory.product.id.eq(productId))
			.fetch();
	}

	@Override
	public List<ProductCategory> findAllWithCategoryByProductIdIn(Collection<Long> productIds) {
		QProductCategory productCategory = QProductCategory.productCategory;
		QCategory category = QCategory.category;

		return queryFactory.selectFrom(productCategory)
			.join(productCategory.category, category).fetchJoin()
			.where(productCategory.product.id.in(productIds))
			.fetch();
	}
}