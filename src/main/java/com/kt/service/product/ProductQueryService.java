package com.kt.service.product;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import com.kt.dto.product.ProductResponse;
import com.kt.repository.category.ProductCategoryRepository;
import com.kt.service.inventory.InventoryService;
import com.kt.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryService {

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final InventoryService inventoryService;

	public Page<ProductResponse.Summary> getPublicProducts(Pageable pageable) {
		var products = productRepository.findNonDeletedByStatuses(activeStatuses(), pageable);
		var categoriesByProduct = loadCategorySummaries(
			products.map(Product::getId).toList()
		);

		return products.map(product -> ProductResponse.Summary.from(
			product,
			inventoryService.getInventoryOrThrow(product.getId()),
			categoriesByProduct.getOrDefault(product.getId(), List.of())
		));
	}

	public ProductResponse.Detail getPublicProduct(Long id) {
		var product = productRepository.findNonDeletedByIdAndStatuses(id, activeStatuses())
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
		var inventory = inventoryService.getInventoryOrThrow(id);
		var categories = loadCategorySummaries(List.of(id)).getOrDefault(id, List.of());
		return ProductResponse.Detail.from(product, inventory, categories);
	}

	private List<ProductStatus> activeStatuses() {
		return List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT);
	}

	private Map<Long, List<ProductResponse.CategorySummary>> loadCategorySummaries(List<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}
		return productCategoryRepository.findAllWithCategoryByProductIdIn(productIds).stream()
			.collect(Collectors.groupingBy(
				pc -> pc.getProduct().getId(),
				Collectors.mapping(pc -> ProductResponse.CategorySummary.from(pc.getCategory()), Collectors.toList())
			));
	}
}