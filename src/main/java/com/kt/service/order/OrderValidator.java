package com.kt.service.order;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.inventory.Inventory;
import com.kt.repository.inventory.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderValidator {

	private final InventoryRepository inventoryRepository;

	// 장바구니 주문 검증
	public void validateCartOrder(List<CartProduct> cartProducts) {
		validateCartNotEmpty(cartProducts);

		for (CartProduct cartProduct : cartProducts) {
			validateStock(cartProduct.getProduct().getId(), cartProduct.getCount());
		}
	}

	// 바로 주문 검증
	public void validateDirectOrder(Long productId, int quantity) {
		validateStock(productId, quantity);
	}

	// 장바구니 비어있는지 검증
	private void validateCartNotEmpty(List<CartProduct> cartProducts) {
		if (cartProducts.isEmpty()) {
			throw new CustomException(ErrorCode.CART_EMPTY);
		}
	}

	// 재고 검증
	private void validateStock(Long productId, int requestedQuantity) {
		Inventory inventory = getInventory(productId);

		// 실제 재고가 1 미만이면 주문 불가
		if (inventory.getPhysicalStockTotal() < 1) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}

		// 요청 수량이 실제 재고보다 많으면 주문 불가
		if (requestedQuantity > inventory.getPhysicalStockTotal()) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}
	}

	private Inventory getInventory(Long productId) {
		return inventoryRepository.findByProductId(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));
	}
}

