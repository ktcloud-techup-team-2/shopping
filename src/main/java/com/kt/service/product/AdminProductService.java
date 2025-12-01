package com.kt.service.product;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.repository.product.ProductRepository;
import com.kt.service.inventory.InventoryService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminProductService {

	private final ProductRepository productRepository;
	private final InventoryService inventoryService;
	private final AuditorAware<Long> auditorAware;

	public ProductResponse.Create create(ProductRequest.Create request) {
		var product = Product.create(
			request.name(),
			request.description(),
			request.price()
		);
		var saved = productRepository.save(product);
		inventoryService.initialize(saved);

		if (request.activateImmediately()) {
			validateActivationStock(saved.getId());
			saved.activate();
		}

		return new ProductResponse.Create(saved.getId());
	}

	@Transactional(readOnly = true)
	public ProductResponse.Detail getDetail(Long id) {
		var product = getProductOrThrow(id);
		var inventory = inventoryService.getInventoryOrThrow(id);
		return ProductResponse.Detail.from(product, inventory);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse.Summary> getPage(Pageable pageable) {
		return productRepository.findNonDeleted(pageable)
			.map(product -> ProductResponse.Summary.from(
				product,
				inventoryService.getInventoryOrThrow(product.getId())
			));
	}

	public ProductResponse.Detail update(Long id, ProductRequest.Update request) {
		var product = getProductOrThrow(id);
		product.update(
			request.name(),
			request.description(),
			request.price()
		);
		var inventory = inventoryService.getInventoryOrThrow(id);
		return ProductResponse.Detail.from(product, inventory);
	}

	public void delete(Long id) {
		var product = productRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		if (product.getStatus() == ProductStatus.DRAFT) {
			product.validateHardDeletable();

			inventoryService.deleteByProductId(product.getId());
			productRepository.delete(product);
			return;
		}

		Long deleterId = auditorAware.getCurrentAuditor().orElse(null);
		product.softDelete(deleterId);
	}

	public ProductResponse.Detail activate(Long id) {
		var product = getProductOrThrow(id);
		validateActivationStock(id);
		product.activate();
		var inventory = inventoryService.getInventoryOrThrow(id);
		return ProductResponse.Detail.from(product, inventory);
	}

	public ProductResponse.Detail inactivate(Long id) {
		var product = getProductOrThrow(id);
		product.inactivate();
		var inventory = inventoryService.getInventoryOrThrow(id);
		return ProductResponse.Detail.from(product, inventory);
	}

	public List<ProductResponse.Detail> markSoldOut(ProductRequest.BulkSoldOut request) {
		var products = productRepository.findAllForUpdateByIds(request.productIds());
		validateAllProductsFound(request.productIds(), products);
		products.forEach(Product::markSoldOut);
		return products.stream()
			.map(product -> ProductResponse.Detail.from(product, inventoryService.getInventoryOrThrow(product.getId())))
			.toList();
	}

	public ProductResponse.Detail toggleSoldOut(Long id) {
		var product = getProductOrThrow(id);
		product.toggleSoldOut();
		var inventory = inventoryService.getInventoryOrThrow(id);
		return ProductResponse.Detail.from(product, inventory);
	}

	private Product getProductOrThrow(Long id) {
		return productRepository.findNonDeletedById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
	}

	private void validateAllProductsFound(List<Long> ids, List<Product> products) {
		Set<Long> foundIds = products.stream()
			.map(Product::getId)
			.collect(Collectors.toSet());
		boolean missing = ids.stream().anyMatch(id -> !foundIds.contains(id));
		if (missing) throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
	}

	private void validateActivationStock(Long productId) {
		if (!inventoryService.hasAvailableStock(productId)) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_REQUIRED_FOR_ACTIVATION);
		}
	}
}
