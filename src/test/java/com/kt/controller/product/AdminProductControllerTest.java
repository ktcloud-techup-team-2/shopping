package com.kt.controller.product;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductQueryRepository;
import com.kt.repository.product.ProductRepository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AdminProductControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/admin/products";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductQueryRepository productQueryRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Nested
	class 상품_생성_API {

		@Test
		void 성공() throws Exception {
			// given
			ProductRequest.Create request = new ProductRequest.Create(
				"테스트 상품명",
				"테스트 상품 설명",
				10_000,
				PetType.DOG,
				false
			);

			var responseBody = new ProductResponse.Create(1L);
			var docsResponse = ApiResponse.of(responseBody);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"admin-products-create", // identifier (스니펫 이름)
						"상품 등록", // summary (swagger summary)
						"관리자 상품 생성 API",  // description
						"Admin-Product", // tag
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 상품_상세_API {

		@Test
		void 성공() throws Exception {
			Product product = createProduct("상세 상품", "상세 상품 설명", 10_000, PetType.DOG, ProductStatus.DRAFT);
			var detail = productQueryRepository.findDetailById(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(detail);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						null,
						HttpMethod.GET,
						objectMapper,
						product.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-products-detail",
						"상품 상세 조회",
						"관리자 상품 상세 조회 API",
						"Admin-Product",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 상품_리스트_API {

		@Test
		void 성공() throws Exception {
			PageRequest pageable = PageRequest.of(0, 10);

			createProduct("리스트 상품1", "리스트 설명1", 10_000, PetType.DOG, ProductStatus.ACTIVE);
			createProduct("리스트 상품2", "리스트 설명2", 20_000, PetType.DOG, ProductStatus.ACTIVE);
			createProduct("리스트 상품3", "리스트 설명3", 30_000, PetType.DOG, ProductStatus.DRAFT);

			var cond = new ProductRequest.SearchCond("리스트 상품", PetType.DOG, ProductStatus.ACTIVE);
			var page = productQueryRepository.findSummaries(cond, pageable);
			var docsResponse = ApiResponse.ofPage(page.getContent(), toPageBlock(page));

			mockMvc.perform(
					restDocsFactory.createParamRequest(
						DEFAULT_URL,
						cond,
						pageable,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-products-list",
						"상품 리스트 조회",
						"관리자 상품 리스트 조회 API",
						"Admin-Product",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 상품_수정_API {

		@Test
		void 성공() throws Exception {
			Product product = createProduct("수정 전 상품", "수정 전 설명", 10_000, PetType.DOG, ProductStatus.DRAFT);
			ProductRequest.Update request = new ProductRequest.Update(
				"수정 후 상품",
				"수정된 설명",
				20_000,
				PetType.DOG
			);

			var perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						request,
						HttpMethod.PUT,
						objectMapper,
						product.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk());

			var updated = productQueryRepository.findDetailById(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(updated);

			perform.andDo(
				restDocsFactory.success(
					"admin-products-update",
					"상품 수정",
					"관리자 상품 수정 API",
					"Admin-Product",
					request,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 상품_활성화_API {

		@Test
		void 성공() throws Exception {
			Product product = createProduct("활성화 대상", "활성화 대상 설명", 10_000, PetType.DOG, ProductStatus.INACTIVE);
			addStock(product, 5);

			var perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}/activate",
						null,
						HttpMethod.POST,
						objectMapper,
						product.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk());

			var updated = productRepository.findByIdAndDeletedFalse(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(ProductResponse.CommandResult.from(updated.getId()));

			perform.andDo(
				restDocsFactory.success(
					"admin-products-activate",
					"상품 활성화",
					"관리자 상품 활성화 API",
					"Admin-Product",
					null,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 상품_비활성화_API {

		@Test
		void 성공() throws Exception {
			Product product = createProduct("비활성화 대상", "비활성화 대상 설명", 10_000, PetType.DOG, ProductStatus.ACTIVE);
			addStock(product, 5);

			var perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}/in-activate",
						null,
						HttpMethod.POST,
						objectMapper,
						product.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk());

			var updated = productRepository.findByIdAndDeletedFalse(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(ProductResponse.CommandResult.from(updated.getId()));

			perform.andDo(
				restDocsFactory.success(
					"admin-products-inactivate",
					"상품 비활성화",
					"관리자 상품 비활성화 API",
					"Admin-Product",
					null,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 상품_품절_일괄_API {

		@Test
		void 성공() throws Exception {
			Product p1 = createProduct("품절 대상1", "설명1", 10_000, PetType.DOG, ProductStatus.ACTIVE);
			Product p2 = createProduct("품절 대상2", "설명2", 20_000, PetType.DOG, ProductStatus.ACTIVE);

			ProductRequest.BulkSoldOut request = new ProductRequest.BulkSoldOut(
				List.of(p1.getId(), p2.getId())
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/sold-out",
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-products-bulk-soldout",
						"상품 일괄 품절 처리",
						"관리자 상품 일괄 품절 처리 API",
						"Admin-Product",
						request,
						null
					)
				);
		}
	}

	@Nested
	class 상품_품절_토글_API {

		@Test
		void 성공() throws Exception {
			Product product = createProduct("품절 토글 대상", "설명", 10_000, PetType.DOG, ProductStatus.ACTIVE);

			var perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}/toggle-sold-out",
						null,
						HttpMethod.POST,
						objectMapper,
						product.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isOk());

			var updated = productRepository.findByIdAndDeletedFalse(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(ProductResponse.CommandResult.from(updated.getId()));

			perform.andDo(
				restDocsFactory.success(
					"admin-products-toggle-soldout",
					"상품 품절 상태 토글",
					"관리자 상품 품절 상태 토글 API",
					"Admin-Product",
					null,
					docsResponse
				)
			);
		}
	}

	@Nested
	class 상품_삭제_API {

		@Test
		void 성공() throws Exception {
			Product product = createProduct("삭제 대상", "삭제 대상 설명", 10_000, PetType.DOG, ProductStatus.ACTIVE);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{id}",
						null,
						HttpMethod.DELETE,
						objectMapper,
						product.getId()
					).with(jwtAdmin())
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"admin-products-delete",
						"상품 삭제",
						"관리자 상품 삭제 API",
						"Admin-Product",
						null,
						null
					)
				);
		}
	}

	private Product createProduct(String name, String description, int price, PetType petType, ProductStatus status) {
		Product product = Product.create(name, description, price, petType);
		Product saved = productRepository.save(product);
		inventoryRepository.save(Inventory.initialize(saved));

		switch (status) {
			case ACTIVE -> saved.activate();
			case INACTIVE -> saved.inactivate();
			case SOLD_OUT -> saved.markSoldOut();
			case DRAFT -> {}
			default -> throw new IllegalArgumentException("지원하지 않는 상태: " + status);
		}
		return saved;
	}

	private void addStock(Product product, long quantity) {
		var inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
		inventory.applyWmsInbound(quantity);
		inventoryRepository.save(inventory);
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
}