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
import com.kt.domain.category.Category;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.repository.category.CategoryRepository;
import com.kt.repository.category.ProductCategoryRepository;
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
	private final CategoryRepository categoryRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final InventoryService inventoryService;

	public ProductResponse.Create create(ProductRequest.Create request) {
		var categories = getCategoriesOrThrow(request.categoryIds(), request.petType());
		var product = Product.create(
			request.name(),
			request.description(),
			request.price(),
			request.petType()
		);
		var saved = productRepository.save(product);
		inventoryService.initialize(saved);
		saveProductCategories(saved, categories);

		if (request.activateImmediately()) {
			validateActivationStock(saved.getId());
			saved.activate();
		}

		return new ProductResponse.Create(saved.getId());
	}

	@Transactional(readOnly = true)
	public ProductResponse.Detail getDetail(Long id) {
		var detail = productQueryRepository.findDetailById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
		var categories = loadCategorySummaries(id);
		return detail.withCategories(categories);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse.Summary> getSummaries(ProductRequest.SearchCond cond, Pageable pageable) {
		var summaries = productQueryRepository.findSummaries(cond, pageable);
		var categoriesByProduct = loadCategorySummaries(
			summaries.map(ProductResponse.Summary::id).toList()
		);
		return summaries.map(summary -> summary.withCategories(
			categoriesByProduct.getOrDefault(summary.id(), List.of())
		));
	}

	public ProductResponse.CommandResult update(Long id, ProductRequest.Update request) {
		var product = getProductOrThrow(id);
		var categories = getCategoriesOrThrow(request.categoryIds(), request.petType());
		product.update(
			request.name(),
			request.description(),
			request.price(),
			request.petType()
		);
		productCategoryRepository.deleteByProductId(id);
		saveProductCategories(product, categories);
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
	private List<Category> getCategoriesOrThrow(List<Long> categoryIds, PetType petType) {
		var categories = categoryRepository.findAllByIdInAndDeletedFalse(categoryIds);
		validateAllCategoriesExist(categoryIds, categories, petType);
		return categories;
	}

	private void validateAllCategoriesExist(List<Long> requestedIds, List<Category> found, PetType petType) {
		if (requestedIds.size() != found.size()) {
			throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
		}

		var categoryPetType = found.isEmpty() ? null : found.getFirst().getPetType();
		boolean invalidPetType = found.stream().anyMatch(c -> c.getPetType() != categoryPetType);
		if (invalidPetType) {
			throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}

		boolean mismatchedWithProduct = categoryPetType != null && categoryPetType != petType;
		if (mismatchedWithProduct) {
			throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}
	}

	private void saveProductCategories(Product product, List<Category> categories) {
		var productCategories = categories.stream()
			.map(category -> com.kt.domain.category.ProductCategory.create(product, category))
			.toList();
		productCategoryRepository.saveAll(productCategories);
	}

	private List<ProductResponse.CategorySummary> loadCategorySummaries(Long productId) {
		return productCategoryRepository.findAllWithCategoryByProductId(productId).stream()
			.map(pc -> ProductResponse.CategorySummary.from(pc.getCategory()))
			.toList();
	}

	private java.util.Map<Long, List<ProductResponse.CategorySummary>> loadCategorySummaries(List<Long> productIds) {
		if (productIds.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		return productCategoryRepository.findAllWithCategoryByProductIdIn(productIds).stream()
			.collect(Collectors.groupingBy(
				pc -> pc.getProduct().getId(),
				Collectors.mapping(pc -> ProductResponse.CategorySummary.from(pc.getCategory()), Collectors.toList())
			));
	}
}
