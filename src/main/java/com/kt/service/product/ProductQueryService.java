package com.kt.service.product;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.product.ProductStatus;
import com.kt.dto.product.ProductResponse;
import com.kt.service.inventory.InventoryService;
import com.kt.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryService {

	private final ProductRepository productRepository;
	private final InventoryService inventoryService;

	public Page<ProductResponse.Summary> getPublicProducts(Pageable pageable) {
		return productRepository.findNonDeletedByStatuses(activeStatuses(), pageable)
			.map(product -> ProductResponse.Summary.from(
				product,
				inventoryService.getInventoryOrThrow(product.getId())
			));
	}

	public ProductResponse.Detail getPublicProduct(Long id) {
		var product = productRepository.findNonDeletedByIdAndStatuses(id, activeStatuses())
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
		var inventory = inventoryService.getInventoryOrThrow(id);
		return ProductResponse.Detail.from(product, inventory);
	}

	private List<ProductStatus> activeStatuses() {
		return List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT);
	}
}