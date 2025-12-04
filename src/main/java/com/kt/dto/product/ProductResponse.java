package com.kt.dto.product;

import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;

public class ProductResponse {
	public record Create(
		Long id
	) {}

	public record Detail(
		Long id,
		String name,
		String description,
		int price,
		long availableQuantity,
		long reservedQuantity,
		long outboundProcessingQuantity,
		ProductStatus status,
		PetType petType,
		boolean deleted
	) {
		public static Detail from(Product product, Inventory inventory) {
			return new Detail(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				inventory.getAvailable(),
				inventory.getReserved(),
				inventory.getOutboundProcessing(),
				product.getStatus(),
				product.getPetType(),
				product.isDeleted()
			);
		}
	}

	public record Summary(
		Long id,
		String name,
		int price,
		long availableQuantity,
		ProductStatus status,
		PetType petType
	) {
		public static Summary from(Product product, Inventory inventory) {
			return new Summary(
				product.getId(),
				product.getName(),
				product.getPrice(),
				inventory.getAvailable(),
				product.getStatus(),
				product.getPetType()
			);
		}
	}

	public record CommandResult(Long id) {
		public static CommandResult from(Long id) {
			return new CommandResult(id);
		}

		public static CommandResult from(Product product) {
			return new CommandResult(product.getId());
		}
	}
}