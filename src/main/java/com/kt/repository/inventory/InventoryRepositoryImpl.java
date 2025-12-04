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

		Inventory result = queryFactory
			.select(inventory)
			.from(inventory)
			.where(product.id.eq(productId))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public Optional<Inventory> findByProductIdForUpdate(Long productId) {
		QInventory inventory = QInventory.inventory;
		QProduct product = QProduct.product;

		Inventory result = queryFactory
			.select(inventory)
			.from(inventory)
			.where(product.id.eq(productId))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public long deleteByProductId(Long productId) {
		QInventory inventory = QInventory.inventory;
		QProduct product = QProduct.product;

		Long inventoryId = queryFactory
			.select(product.id)
			.from(product)
			.where(product.id.eq(productId))
			.fetchOne();

		if (inventoryId == null) {
			return 0L;
		}

		return queryFactory
			.delete(inventory)
			.where(inventory.id.eq(inventoryId))
			.execute();
	}
}