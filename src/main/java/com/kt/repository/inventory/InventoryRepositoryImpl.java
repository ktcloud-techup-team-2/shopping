package com.kt.repository.inventory;

import com.kt.domain.inventory.Inventory;
import com.kt.domain.inventory.QInventory;
import com.kt.domain.product.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<Inventory> findByProductId(Long productId) {
		QInventory inventory = QInventory.inventory;
		QProduct product = QProduct.product;

		Inventory result = queryFactory.selectFrom(inventory)
			.join(inventory.product, product).fetchJoin()
			.where(product.id.eq(productId))
			.fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<Inventory> findByProductIdForUpdate(Long productId) {
		QInventory inventory = QInventory.inventory;
		QProduct product = QProduct.product;

		Inventory result = queryFactory.selectFrom(inventory)
			.join(inventory.product, product).fetchJoin()
			.where(product.id.eq(productId))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();
		return Optional.ofNullable(result);
	}
}