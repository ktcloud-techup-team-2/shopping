package com.kt.service.category;

import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import com.kt.dto.category.CategoryResponse;
import com.kt.dto.tree.TreeMapper;
import com.kt.repository.category.CategoryRepository;
import com.kt.repository.category.ProductCategoryRepository;
import com.kt.repository.product.ProductRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryQueryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;

	public CategoryResponse.UserLevels getLevels(PetType petType) {
		List<Category> categories = categoryRepository.findAllByStatusAndPetType(CategoryStatus.ACTIVE, petType);

		Map<Integer, List<CategoryResponse.UserLevels.UserCategory>> grouped = new LinkedHashMap<>();
		for (Category category : categories) {
			grouped.computeIfAbsent(category.getDepth(), d -> new ArrayList<>())
				.add(new CategoryResponse.UserLevels.UserCategory(
					category.getId(),
					category.getName(),
					category.getParent() == null ? null : category.getParent().getId(),
					category.getPetType(),
					category.getSortOrder()
				));
		}

		List<CategoryResponse.UserLevels.Level> levels = grouped.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new CategoryResponse.UserLevels.Level(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		return new CategoryResponse.UserLevels(levels);
	}
	public CategoryResponse.Tree getUserTree(PetType petType) {
		List<Category> categories = categoryRepository.findAllByStatusAndPetType(CategoryStatus.ACTIVE, petType);
		List<Product> products = productRepository.findNonDeletedByStatusesAndPetType(activeProductStatuses(), petType);

		Map<Long, Product> productMap = products.stream()
			.collect(Collectors.toMap(Product::getId, Function.identity()));

		List<TreeSource> sources = new ArrayList<>();
		for (Category category : categories) {
			sources.add(TreeSource.category(category));
		}

		if (!productMap.isEmpty()) {
			var productCategories = productCategoryRepository.findAllWithCategoryByProductIdIn(productMap.keySet());
			for (var pc : productCategories) {
				var product = productMap.get(pc.getProduct().getId());
				if (product == null) continue;
				sources.add(TreeSource.product(pc.getCategory().getId(), product));
			}
		}

		var tree = TreeMapper.toTreeResponse(
			sources,
			TreeSource::id,
			TreeSource::label,
			TreeSource::parentId,
			TreeSource::sortOrder,
			TreeSource::keywords
		);

		return CategoryResponse.Tree.builder().tree(tree).build();
	}

	private List<ProductStatus> activeProductStatuses() {
		return List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT);
	}

	private record TreeSource(String id, String label, String parentId, Integer sortOrder, String keywords) {
		private static TreeSource category(Category category) {
			return new TreeSource(
				String.valueOf(category.getId()),
				category.getName(),
				category.getParent() == null ? null : String.valueOf(category.getParent().getId()),
				category.getSortOrder(),
				category.getName()
			);
		}

		private static TreeSource product(Long categoryId, Product product) {
			return new TreeSource(
				"P" + product.getId() + "-C" + categoryId,
				product.getName(),
				String.valueOf(categoryId),
				Integer.MAX_VALUE,
				product.getDescription()
			);
		}
	}
}