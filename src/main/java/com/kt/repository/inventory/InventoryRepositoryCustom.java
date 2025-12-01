package com.kt.repository.inventory;

import com.kt.domain.inventory.Inventory;
import java.util.Optional;

public interface InventoryRepositoryCustom {

	Optional<Inventory> findByProductId(Long productId);

	Optional<Inventory> findByProductIdForUpdate(Long productId);

	long deleteByProductId(Long productId);
}