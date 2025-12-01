package com.kt.service.inventory;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.product.Product;
import com.kt.repository.inventory.InventoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryRepository inventoryRepository;

	@Transactional
	public Inventory initialize(Product product) {
		return inventoryRepository.save(Inventory.initialize(product));
	}

	@Transactional(readOnly = true)
	public Inventory getInventoryOrThrow(Long productId) {
		return inventoryRepository.findByProductId(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public boolean hasAvailableStock(Long productId) {
		return getInventoryOrThrow(productId).hasAvailableStock();
	}
}