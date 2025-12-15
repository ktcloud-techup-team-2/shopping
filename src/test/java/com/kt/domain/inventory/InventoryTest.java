package com.kt.domain.inventory;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

	@Test
	void WMS_입고로_재고가_증가한다() {
		Inventory inventory = newInventoryWithProduct();
		inventory.applyWmsInbound(5);

		assertThat(inventory.getPhysicalStockTotal()).isEqualTo(5);
		assertThat(inventory.getAvailable()).isEqualTo(5);
	}

	@Test
	void OMS_RESERVE는_가용재고를_차감한다() {
		Inventory inventory = newInventoryWithProduct();
		inventory.applyWmsInbound(10);

		inventory.applyOmsReserve(3);

		assertThat(inventory.getReserved()).isEqualTo(3);
		assertThat(inventory.getAvailable()).isEqualTo(7);
	}

	@Test
	void OMS_COMMIT은_예약재고를_출고준비로_이동한다() {
		Inventory inventory = newInventoryWithProduct();
		inventory.applyWmsInbound(10);
		inventory.applyOmsReserve(4);

		inventory.applyOmsCommit(2);

		assertThat(inventory.getReserved()).isEqualTo(2);
		assertThat(inventory.getOutboundProcessing()).isEqualTo(2);
		assertThat(inventory.getAvailable()).isEqualTo(6);
	}

	@Test
	void 예약보다_많이_커밋하면_예외가_발생한다() {
		Inventory inventory = newInventoryWithProduct();
		inventory.applyWmsInbound(3);
		inventory.applyOmsReserve(1);

		assertThatThrownBy(() -> inventory.applyOmsCommit(2))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_RESERVATION_NOT_FOUND);
	}

	private Inventory newInventoryWithProduct() {
		Product product = Product.create("테스트", "설명", 10_000, PetType.DOG);
		return Inventory.initialize(product);
	}
}