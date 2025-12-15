package com.kt.repository.inventory;

import com.kt.domain.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, InventoryRepositoryCustom {
}