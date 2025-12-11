package com.kt.controller.category;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.category.ProductCategory;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.dto.category.CategoryResponse;
import com.kt.repository.category.CategoryRepository;
import com.kt.repository.category.ProductCategoryRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.service.category.CategoryQueryService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CategoryControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/categories";

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@MockitoBean
	private RedissonClient redissonClient;

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Autowired
	private CategoryQueryService categoryQueryService;

	@Test
	void 분류_레벨_리스트_API_성공() throws Exception {
		// given
		Category root = categoryRepository.save(
			Category.createRoot("루트", 1, CategoryStatus.ACTIVE, PetType.DOG)
		);
		categoryRepository.save(
			Category.createChild(root, "자식1", 1, CategoryStatus.ACTIVE, PetType.DOG)
		);
		categoryRepository.save(
			Category.createChild(root, "자식2", 2, CategoryStatus.ACTIVE, PetType.DOG)
		);
		categoryRepository.save(
			Category.createRoot("다른 분류", 3, CategoryStatus.INACTIVE, PetType.DOG)
		);

		var levels = buildLevels();
		var docsResponse = ApiResponse.of(new CategoryResponse.UserLevels(levels));

		// when & then
		mockMvc.perform(
				restDocsFactory.createParamRequest(
					DEFAULT_URL + "/levels",
					new PetTypeParam(PetType.DOG),
					null,
					objectMapper
				).with(jwtUser())
			)
			.andExpect(status().isOk())
			.andDo(
				restDocsFactory.successWithRequestParameters(
					"categories-levels",
					"카테고리 레벨 조회",
					"사용자 카테고리 레벨별 조회 API",
					"Category",
					new PetTypeParam(PetType.DOG),
					null,
					objectMapper,
					docsResponse
				)
			);
	}

	@Test
	void 카테고리_트리_API_성공() throws Exception {
		Category root = categoryRepository.save(
			Category.createRoot("루트", 1, CategoryStatus.ACTIVE, PetType.DOG)
		);
		Category child = categoryRepository.save(
			Category.createChild(root, "자식", 1, CategoryStatus.ACTIVE, PetType.DOG)
		);

		Product product = productRepository.save(Product.create("상품", "상품 설명", 10_000, PetType.DOG));
		Inventory inventory = inventoryRepository.save(Inventory.initialize(product));
		inventory.applyWmsInbound(3);
		inventoryRepository.save(inventory);
		product.activate();
		productRepository.save(product);

		productCategoryRepository.save(ProductCategory.create(product, child));

		var tree = categoryQueryService.getUserTree(PetType.DOG);
		var docsResponse = ApiResponse.of(tree);

		mockMvc.perform(
				restDocsFactory.createParamRequest(
					DEFAULT_URL + "/tree",
					new PetTypeParam(PetType.DOG),
					null,
					objectMapper
				).with(jwtUser())
			)
			.andExpect(status().isOk())
			.andDo(
				restDocsFactory.successWithRequestParameters(
					"categories-tree",
					"카테고리 트리 조회",
					"사용자 카테고리 트리 조회 API",
					"Category",
					new PetTypeParam(PetType.DOG),
					null,
					objectMapper,
					docsResponse
				)
			);
	}

	private List<CategoryResponse.UserLevels.Level> buildLevels() {
		List<Category> categories = categoryRepository.findAllByStatusAndPetType(CategoryStatus.ACTIVE, PetType.DOG);
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

		return grouped.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new CategoryResponse.UserLevels.Level(entry.getKey(), entry.getValue()))
			.toList();
	}

	private record PetTypeParam(PetType petType) {}
}