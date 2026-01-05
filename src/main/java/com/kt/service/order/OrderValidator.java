package com.kt.service.order;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.repository.inventory.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderValidator {

	private final InventoryRepository inventoryRepository;

	// 장바구니 주문 검증 (장바구니에 담긴 여러 상품들이 주문 가능한지 검증)
	public void validateCartOrder(List<CartProduct> cartProducts) {
		validateCartNotEmpty(cartProducts);

		for (CartProduct cartProduct : cartProducts) {
			validateStock(cartProduct.getProduct().getId(), cartProduct.getCount());
		}
	}

	// 바로 주문 검증 (단일 상품에 대해서 주문 가능한지 검증)
	public void validateDirectOrder(Long productId, int quantity) {
		validateStock(productId, quantity);
	}

	// 장바구니 비어있는지 검증
	private void validateCartNotEmpty(List<CartProduct> cartProducts) {
		if (cartProducts.isEmpty()) {
			throw new CustomException(ErrorCode.CART_EMPTY);
		}
	}

	// 재고 검증 (품절이 됐는지, 잔여재고가 충분한지 검증)
	private void validateStock(Long productId, int requestedQuantity) {
		Inventory inventory = getInventory(productId);

		// 실제 재고가 1 미만이면 주문 불가 = 품절인 경우
		if (inventory.getPhysicalStockTotal() < 1) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}

		// 요청 수량이 실제 재고보다 많으면 주문 불가 = 재고가 적은 경우
		if (requestedQuantity > inventory.getPhysicalStockTotal()) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}
	}

	// 주문 상품 재검증 (이미 생성된 주문에 대해서 재고를 검증)
	public void validateOrderProducts(List<OrderProduct> orderProducts) {
		for (OrderProduct orderProduct : orderProducts) {
			validateStock(orderProduct.getProductId(), orderProduct.getQuantity());
		}
	}

	private Inventory getInventory(Long productId) {
		return inventoryRepository.findByProductId(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));
	}
}

