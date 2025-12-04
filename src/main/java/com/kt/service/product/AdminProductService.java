package com.kt.service.product;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.kt.repository.product.ProductQueryRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.security.AuthUser;
import com.kt.service.inventory.InventoryService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminProductService {

	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
	private final InventoryService inventoryService;

	public ProductResponse.Create create(ProductRequest.Create request) {
		var product = Product.create(
			request.name(),
			request.description(),
			request.price(),
			request.petType()
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
		return productQueryRepository.findDetailById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse.Summary> getSummaries(ProductRequest.SearchCond cond, Pageable pageable) {
		return productQueryRepository.findSummaries(cond, pageable);
	}

	public ProductResponse.CommandResult update(Long id, ProductRequest.Update request) {
		var product = getProductOrThrow(id);
		product.update(
			request.name(),
			request.description(),
			request.price(),
			request.petType()
		);
		return ProductResponse.CommandResult.from(product);
	}

	public void delete(Long id, AuthUser authUser) {
		var product = productRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		if (product.getStatus() == ProductStatus.DRAFT) {
			product.validateHardDeletable();
			productRepository.delete(product);
			return;
		}
		product.softDelete(authUser.id());
	}

	public ProductResponse.CommandResult activate(Long id) {
		var product = getProductOrThrow(id);
		validateActivationStock(id);
		product.activate();
		return ProductResponse.CommandResult.from(product);
	}

	public ProductResponse.CommandResult inactivate(Long id) {
		var product = getProductOrThrow(id);
		product.inactivate();
		return ProductResponse.CommandResult.from(product);
	}

	public void markSoldOut(ProductRequest.BulkSoldOut request, AuthUser authUser) {
		var products = productRepository.findByIdInAndDeletedFalse(request.productIds());
		validateAllProductsFound(request.productIds(), products);
		productRepository.bulkMarkSoldOut(request.productIds(), authUser.id());
	}

	public ProductResponse.CommandResult toggleSoldOut(Long id) {
		var product = getProductOrThrow(id);
		product.toggleSoldOut();
		return ProductResponse.CommandResult.from(product);
	}

	private Product getProductOrThrow(Long id) {
		return productRepository.findByIdAndDeletedFalse(id)
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
