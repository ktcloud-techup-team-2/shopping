package com.kt.controller.cart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.dto.cart.CartRequest;
import com.kt.repository.cart.CartProductRepository;
import com.kt.repository.cart.CartRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 컨트롤러 테스트
 * 
 * 테스트 시나리오:
 * 1. 장바구니에 상품 추가
 * 2. 장바구니 조회
 * 3. 장바구니 상품 삭제
 * 4. 장바구니 상품 수량 변경
 */
@Transactional
class CartControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/cart";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartProductRepository cartProductRepository;

	// ==================== 장바구니 상품 추가 ====================

	@Nested
	@DisplayName("장바구니 상품 추가 API")
	class 장바구니_상품_추가_API {

		@Test
		@DisplayName("성공: 새 상품을 장바구니에 추가")
		void 성공() throws Exception {
			// given
			Product product = createProduct("강아지 장난감", "재미있는 장난감", 12000, PetType.DOG);

			CartRequest.Add request = new CartRequest.Add(
				product.getId(),
				2
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/cart-products",
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.name").value("강아지 장난감"))
				.andExpect(jsonPath("$.data.count").value(2))
				.andDo(
					restDocsFactory.success(
						"cart-add-product",
						"장바구니 상품 추가",
						"장바구니에 새 상품을 추가하는 API",
						"Cart",
						request,
						null
					)
				);
		}

		@Test
		@DisplayName("성공: 이미 담긴 상품 수량 증가")
		void 이미_담긴_상품_수량_증가() throws Exception {
			// given: 이미 장바구니에 상품이 담겨있음
			Product product = createProduct("고양이 츄르", "맛있는 간식", 5000, PetType.CAT);
			Cart cart = createCart(DEFAULT_USER_ID);
			createCartProduct(cart, product, 2);

			CartRequest.Add request = new CartRequest.Add(
				product.getId(),
				3  // 추가로 3개 담기
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/cart-products",
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.count").value(5))  // 2 + 3 = 5
				.andDo(
					restDocsFactory.success(
						"cart-add-existing-product",
						"장바구니 기존 상품 수량 증가",
						"이미 담긴 상품의 수량을 증가시키는 API",
						"Cart",
						request,
						null
					)
				);
		}
	}

	// ==================== 장바구니 조회 ====================

	@Nested
	@DisplayName("장바구니 조회 API")
	class 장바구니_조회_API {

		@Test
		@DisplayName("성공: 장바구니 상품 목록 조회")
		void 성공() throws Exception {
			// given
			Product product1 = createProduct("강아지 사료", "영양만점 사료", 25000, PetType.DOG);
			Product product2 = createProduct("강아지 간식", "맛있는 간식", 8000, PetType.DOG);
			
			Cart cart = createCart(DEFAULT_USER_ID);
			createCartProduct(cart, product1, 2);
			createCartProduct(cart, product2, 5);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(2))
				.andDo(
					restDocsFactory.success(
						"cart-list",
						"장바구니 조회",
						"장바구니에 담긴 상품 목록을 조회하는 API",
						"Cart",
						null,
						null
					)
				);
		}

		@Test
		@DisplayName("성공: 빈 장바구니 조회")
		void 빈_장바구니_조회() throws Exception {
			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(0))
				.andDo(
					restDocsFactory.success(
						"cart-empty",
						"빈 장바구니 조회",
						"비어있는 장바구니를 조회하는 API",
						"Cart",
						null,
						null
					)
				);
		}
	}

	// ==================== 장바구니 상품 삭제 ====================

	@Nested
	@DisplayName("장바구니 상품 삭제 API")
	class 장바구니_상품_삭제_API {

		@Test
		@DisplayName("성공: 장바구니에서 상품 삭제")
		void 성공() throws Exception {
			// given
			Product product = createProduct("삭제할 상품", "설명", 10000, PetType.DOG);
			Cart cart = createCart(DEFAULT_USER_ID);
			CartProduct cartProduct = createCartProduct(cart, product, 1);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/cart-products/{id}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							cartProduct.getId()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"cart-delete-product",
						"장바구니 상품 삭제",
						"장바구니에서 특정 상품을 삭제하는 API",
						"Cart",
						null,
						null
					)
				);
		}
	}

	// ==================== 장바구니 상품 수량 변경 ====================

	@Nested
	@DisplayName("장바구니 상품 수량 변경 API")
	class 장바구니_상품_수량_변경_API {

		@Test
		@DisplayName("성공: 상품 수량 변경")
		void 성공() throws Exception {
			// given
			Product product = createProduct("수량 변경 상품", "설명", 15000, PetType.CAT);
			Cart cart = createCart(DEFAULT_USER_ID);
			CartProduct cartProduct = createCartProduct(cart, product, 2);

			CartRequest.CountUpdate request = new CartRequest.CountUpdate(5);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/cart-products/{id}",
							request,
							HttpMethod.PUT,
							objectMapper,
							cartProduct.getId()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.previousCount").value(2))
				.andExpect(jsonPath("$.data.newCount").value(5))
				.andExpect(jsonPath("$.data.message").exists())
				.andDo(
					restDocsFactory.success(
						"cart-update-count",
						"장바구니 상품 수량 변경",
						"장바구니에 담긴 상품의 수량을 변경하는 API",
						"Cart",
						request,
						null
					)
				);
		}
	}

	// ==================== 헬퍼 메서드 ====================

	private Product createProduct(String name, String description, int price, PetType petType) {
		Product product = productRepository.save(Product.create(name, description, price, petType));

		Inventory inventory = Inventory.initialize(product);
		inventory.applyWmsInbound(100);
		inventoryRepository.save(inventory);

		product.activate();
		productRepository.save(product);

		return product;
	}

	private Cart createCart(Long userId) {
		Cart cart = Cart.create(userId);
		return cartRepository.save(cart);
	}

	private CartProduct createCartProduct(Cart cart, Product product, int count) {
		CartProduct cartProduct = CartProduct.create(cart, product, count);
		return cartProductRepository.save(cartProduct);
	}
}
