package com.kt.controller.product;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.category.ProductCategory;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.dto.product.ProductResponse;
import com.kt.repository.category.CategoryRepository;
import com.kt.repository.category.ProductCategoryRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ProductControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/products";

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@MockitoBean
	private RedissonClient redissonClient;

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Nested
	class 상품_리스트_API {
		@Test
		void 성공() throws Exception {
			PageRequest pageable = PageRequest.of(0, 10);
			Category dogCategory = createCategory("강아지 카테고리", PetType.DOG);
			Category catCategory = createCategory("고양이 카테고리", PetType.CAT);

			Product first = createActiveProduct("공개 상품1", "공개 상품 설명1", 5_000, PetType.DOG);
			Product second = createActiveProduct("공개 상품2", "공개 상품 설명2", 15_000, PetType.CAT);

			productCategoryRepository.save(ProductCategory.create(first, dogCategory));
			productCategoryRepository.save(ProductCategory.create(second, catCategory));

			Page<Product> page = new PageImpl<>(java.util.List.of(first, second), pageable, 2);
			Map<Long, List<ProductResponse.CategorySummary>> categoriesByProduct = Map.of(
				first.getId(), List.of(ProductResponse.CategorySummary.from(dogCategory)),
				second.getId(), List.of(ProductResponse.CategorySummary.from(catCategory))
			);
			var summaries = page.map(p -> ProductResponse.Summary.from(
					p,
					inventoryRepository.findByProductId(p.getId()).orElseThrow(),
					categoriesByProduct.getOrDefault(p.getId(), List.of())
				))
				.getContent();
			var docsResponse = ApiResponse.ofPage(summaries, toPageBlock(page));

			mockMvc.perform(
				restDocsFactory.createParamRequest(
					DEFAULT_URL,
					null,
					pageable,
					objectMapper
				).with(jwtUser())
			)
			.andExpect(status().isOk())
			.andDo(
				restDocsFactory.successWithRequestParameters(
					"products-list",
					"상품 리스트 조회",
					"사용자 상품 리스트 조회 API",
					"Product",
					null,
					pageable,
					objectMapper,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 상품_상세_API {
		@Test
		void 성공() throws Exception {
			Category category = createCategory("상세 카테고리", PetType.DOG);
			Product product = createActiveProduct("상세용 공개 상품", "공개 상품 설명", 5_000, PetType.DOG);
			productCategoryRepository.save(ProductCategory.create(product, category));
			var inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(
				ProductResponse.Detail.from(
					product,
					inventory,
					List.of(ProductResponse.CategorySummary.from(category))
				)
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						null,
						HttpMethod.GET,
						objectMapper,
						product.getId()
					).with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"products-detail",
						"상품 상세 조회",
						"사용자 상품 상세 조회 API",
						"Product",
						null,
						docsResponse
					)
				);
		}
	}

	private PageBlock toPageBlock(Page<?> page) {
		return new PageBlock(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext(),
			page.hasPrevious(),
			page.getSort().stream()
				.map(order -> new PageBlock.SortOrder(order.getProperty(), order.getDirection().name()))
				.toList()
		);
	}

	private Product createActiveProduct(String name, String description, int price, PetType petType) {
		Product product = productRepository.save(Product.create(name, description, price, petType));
		Inventory inventory = Inventory.initialize(product);
		inventory.applyWmsInbound(10);
		inventoryRepository.save(inventory);

		product.activate();
		productRepository.save(product);

		return product;
	}

	private Category createCategory(String name, PetType petType) {
		return categoryRepository.save(Category.createRoot(name, 1, CategoryStatus.ACTIVE, petType));
	}
}