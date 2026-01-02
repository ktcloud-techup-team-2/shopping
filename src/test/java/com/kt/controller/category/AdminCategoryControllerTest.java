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
import com.kt.dto.category.CategoryRequest;
import com.kt.dto.category.CategoryResponse;
import com.kt.dto.tree.TreeMapper;
import com.kt.repository.category.CategoryRepository;
import com.kt.repository.category.ProductCategoryRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AdminCategoryControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/admin/categories";

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

	@Nested
	class 카테고리_생성_API {
		@Test
		void 성공() throws Exception {
			// given
			CategoryRequest.Create request = new CategoryRequest.Create(
				"신규 카테고리",
				null,
				0,
				CategoryStatus.ACTIVE,
				PetType.DOG
			);

			// when
			var perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isCreated());

			Category created = categoryRepository.findAll().getFirst();
			var docsResponse = ApiResponse.of(CategoryResponse.Detail.from(created));

			perform.andDo(
				restDocsFactory.success(
					"admin-categories-create",
					"카테고리 등록",
					"관리자 카테고리 생성 API",
					"Admin-Category",
					request,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 카테고리_상세_API {
		@Test
		void 성공() throws Exception {
			// given
			Category category = categoryRepository.save(
				Category.createRoot("상세 카테고리", 1, CategoryStatus.ACTIVE, PetType.CAT)
			);

			var docsResponse = ApiResponse.of(CategoryResponse.Detail.from(category));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						null,
						HttpMethod.GET,
						objectMapper,
						category.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-categories-detail",
						"카테고리 상세 조회",
						"관리자 카테고리 상세 조회 API",
						"Admin-Category",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 카테고리_수정_API {
		@Test
		void 성공() throws Exception {
			// given
			Category parent = categoryRepository.save(
				Category.createRoot("부모", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);
			Category category = categoryRepository.save(
				Category.createChild(parent, "수정 대상", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);

			CategoryRequest.Update request = new CategoryRequest.Update(
				"수정된 이름",
				parent.getId(),
				3,
				CategoryStatus.INACTIVE,
				PetType.DOG
			);

			// when
			var perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						request,
						HttpMethod.PUT,
						objectMapper,
						category.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk());

			Category updated = categoryRepository.findById(category.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(CategoryResponse.Detail.from(updated));

			perform.andDo(
				restDocsFactory.success(
					"admin-categories-update",
					"카테고리 수정",
					"관리자 카테고리 수정 API",
					"Admin-Category",
					request,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 카테고리_삭제_API {
		@Test
		void 성공() throws Exception {
			// given
			Category category = categoryRepository.save(
				Category.createRoot("삭제 카테고리", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						null,
						HttpMethod.DELETE,
						objectMapper,
						category.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"admin-categories-delete",
						"카테고리 삭제",
						"관리자 카테고리 삭제 API",
						"Admin-Category",
						null,
						null
					)
				);
		}

		@Test
		void 하위_상품이_있으면_실패() throws Exception {
			Category root = categoryRepository.save(
				Category.createRoot("삭제 불가 루트", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);
			Category child = categoryRepository.save(
				Category.createChild(root, "자식", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);

			Product product = productRepository.save(Product.create("상품", "설명", 1_000, PetType.DOG));
			Inventory inventory = inventoryRepository.save(Inventory.initialize(product));
			inventory.applyWmsInbound(1);
			inventoryRepository.save(inventory);
			product.activate();
			productRepository.save(product);
			productCategoryRepository.save(ProductCategory.create(product, child));

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						null,
						HttpMethod.DELETE,
						objectMapper,
						root.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isConflict());
		}
	}

	@Nested
	class 카테고리_트리_API {
		@Test
		void 성공() throws Exception {
			// given
			Category root = categoryRepository.save(
				Category.createRoot("루트", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);
			categoryRepository.save(
				Category.createChild(root, "자식", 1, CategoryStatus.ACTIVE, PetType.DOG)
			);
			categoryRepository.save(
				Category.createRoot("다른 루트", 2, CategoryStatus.INACTIVE, PetType.DOG)
			);

			var tree = TreeMapper.fromCategories(categoryRepository.findAllForAdmin(PetType.DOG));
			var docsResponse = ApiResponse.of(CategoryResponse.Tree.builder().tree(tree).build());

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/tree",
						null,
						HttpMethod.GET,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-categories-tree",
						"카테고리 트리 조회",
						"관리자 카테고리 트리 조회 API",
						"Admin-Category",
						null,
						docsResponse
					)
				);
		}
	}
}