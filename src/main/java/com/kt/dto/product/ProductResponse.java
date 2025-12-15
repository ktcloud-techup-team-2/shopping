package com.kt.dto.product;

import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.category.Category;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;

import java.util.Collections;
import java.util.List;

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
		boolean deleted,
		List<CategorySummary> categories
	) {
		public Detail {
			categories = categories != null ? categories : Collections.emptyList();
		}

		public static Detail from(Product product, Inventory inventory, List<CategorySummary> categories) {
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
				product.isDeleted(),
				categories
			);
		}

		public Detail withCategories(List<CategorySummary> categories) {
			return new Detail(
				id,
				name,
				description,
				price,
				availableQuantity,
				reservedQuantity,
				outboundProcessingQuantity,
				status,
				petType,
				deleted,
				categories != null ? categories : Collections.emptyList()
			);
		}
	}

	public record Summary(
		Long id,
		String name,
		int price,
		long availableQuantity,
		ProductStatus status,
		PetType petType,
		List<CategorySummary> categories
	) {
		public Summary {
			categories = categories != null ? categories : Collections.emptyList();
		}

		public static Summary from(Product product, Inventory inventory, List<CategorySummary> categories) {
			return new Summary(
				product.getId(),
				product.getName(),
				product.getPrice(),
				inventory.getAvailable(),
				product.getStatus(),
				product.getPetType(),
				categories
			);
		}

		public Summary withCategories(List<CategorySummary> categories) {
			return new Summary(
				id,
				name,
				price,
				availableQuantity,
				status,
				petType,
				categories != null ? categories : Collections.emptyList()
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

	public record CategorySummary(Long id, String name, Integer depth) {
		public static CategorySummary from(Category category) {
			return new CategorySummary(category.getId(), category.getName(), category.getDepth());
		}
	}
}