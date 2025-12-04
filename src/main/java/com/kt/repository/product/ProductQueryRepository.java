package com.kt.repository.product;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.kt.domain.inventory.QInventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.ProductStatus;
import com.kt.domain.product.QProduct;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Optional<ProductResponse.Detail> findDetailById(Long id) {
		QProduct product = QProduct.product;
		QInventory inventory = QInventory.inventory;

		var result = queryFactory
			.select(Projections.constructor(
				ProductResponse.Detail.class,
				product.id,
				product.name,
				product.description,
				product.price,
				inventory.available,
				inventory.reserved,
				inventory.outboundProcessing,
				product.status,
				product.petType,
				product.deleted
			))
			.from(product)
			.leftJoin(inventory).on(inventory.product.eq(product))
			.where(product.id.eq(id)
				.and(product.deleted.isFalse()))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	public Page<ProductResponse.Summary> findSummaries(ProductRequest.SearchCond cond, Pageable pageable) {
		QProduct product = QProduct.product;
		QInventory inventory = QInventory.inventory;

		var conditions = buildConditions(cond);

		var content = queryFactory
			.select(Projections.constructor(
				ProductResponse.Summary.class,
				product.id,
				product.name,
				product.price,
				inventory.available,
				product.status,
				product.petType
			))
			.from(product)
			.leftJoin(inventory).on(inventory.product.eq(product))
			.where(conditions)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		var total = queryFactory
			.select(product.count())
			.from(product)
			.where(conditions)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	private BooleanExpression buildConditions(ProductRequest.SearchCond cond) {
		return Expressions.allOf(
			QProduct.product.deleted.isFalse(),
			nameContains(cond.name()),
			petTypeEq(cond.petType()),
			statusEq(cond.status())
		);
	}

	private BooleanExpression nameContains(String name) {
		return Strings.isNotBlank(name)
			? QProduct.product.name.contains(name)
			: null;
	}

	private BooleanExpression petTypeEq(PetType petType) {
		return petType != null
			? QProduct.product.petType.eq(petType)
			: null;
	}

	private BooleanExpression statusEq(ProductStatus status) {
		return status != null
			? QProduct.product.status.eq(status)
			: null;
	}
}
