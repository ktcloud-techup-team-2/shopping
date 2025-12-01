package com.kt.controller.product;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.product.Product;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
			Product product = createDraftProduct("상세 상품", "상세 상품 설명", 10_000);
			var inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(ProductResponse.Detail.from(product, inventory));

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
			Product first = createDraftProduct("리스트 상품1", "리스트 설명1", 10_000);
			Product second = createDraftProduct("리스트 상품2", "리스트 설명2", 20_000);

			Page<Product> page = new PageImpl<>(java.util.List.of(first, second), pageable, 2);
			var summaries = page.map(p -> ProductResponse.Summary.from(
					p,
					inventoryRepository.findByProductId(p.getId()).orElseThrow()
				))
				.getContent();
			var docsResponse = ApiResponse.ofPage(summaries, toPageBlock(page));

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						null,
						HttpMethod.GET,
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
			Product product = createDraftProduct("수정 전 상품", "수정 전 설명", 10_000);
			ProductRequest.Update request = new ProductRequest.Update(
				"수정 후 상품",
				"수정된 설명",
				20_000
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

			var updated = productRepository.findById(product.getId()).orElseThrow();
			var inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(ProductResponse.Detail.from(updated, inventory));

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
			Product product = createDraftProduct("활성화 대상", "활성화 대상 설명", 10_000);
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

			var updated = productRepository.findById(product.getId()).orElseThrow();
			var inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
			var docsResponse = ApiResponse.of(ProductResponse.Detail.from(updated, inventory));

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
	class 상품_삭제_API {

		@Test
		void 성공() throws Exception {
			Product product = createDraftProduct("삭제 대상", "삭제 대상 설명", 10_000);

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

	private Product createDraftProduct(String name, String description, int price) {
		Product product = Product.create(name, description, price);
		Product saved = productRepository.save(product);
		inventoryRepository.save(Inventory.initialize(saved));
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