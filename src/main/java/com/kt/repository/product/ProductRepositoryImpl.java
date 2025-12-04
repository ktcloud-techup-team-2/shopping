package com.kt.repository.product;

import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import com.kt.domain.product.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Product> findNonDeletedByStatuses(Collection<ProductStatus> statuses, Pageable pageable) {
		QProduct product = QProduct.product;

		List<Product> content = queryFactory.selectFrom(product)
			.where(product.deleted.isFalse()
				.and(product.status.in(statuses)))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		return PageableExecutionUtils.getPage(
			content,
			pageable,
			() -> queryFactory.select(product.count())
				.from(product)
				.where(product.deleted.isFalse()
					.and(product.status.in(statuses)))
				.fetchOne()
		);
	}

	@Override
	public Optional<Product> findNonDeletedByIdAndStatuses(Long id, Collection<ProductStatus> statuses) {
		QProduct product = QProduct.product;
		Product result = queryFactory.selectFrom(product)
			.where(product.id.eq(id)
				.and(product.deleted.isFalse())
				.and(product.status.in(statuses)))
			.fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public long bulkMarkSoldOut(Collection<Long> ids, Long userId) {
		QProduct p = QProduct.product;

		return queryFactory
			.update(p)
			.set(p.status, ProductStatus.SOLD_OUT)
			.set(p.updatedAt, LocalDateTime.now())
			.set(p.updatedBy, userId)
			.where(p.id.in(ids)
				.and(p.deleted.isFalse()))
			.execute();
	}
}