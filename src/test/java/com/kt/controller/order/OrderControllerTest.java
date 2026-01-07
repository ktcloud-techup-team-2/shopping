package com.kt.controller.order;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.order.Order;
import com.kt.domain.order.OrderType;
import com.kt.domain.order.Receiver;
import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentType;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.dto.order.OrderRequest;
import com.kt.repository.cart.CartProductRepository;
import com.kt.repository.cart.CartRepository;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.payment.PaymentRepository;
import com.kt.repository.product.ProductRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 컨트롤러 테스트
 *
 * 테스트 시나리오:
 * 1. 바로 주문 생성 (상품 페이지에서 직접 주문)
 * 2. 장바구니 주문 생성 (장바구니에서 주문)
 * 3. 주문 목록 조회
 * 4. 주문 상세 조회
 * 5. 주문 취소 (결제 전)
 * 6. 주문 정보 수정
 */
@Transactional
class OrderControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/orders";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private DeliveryAddressRepository deliveryAddressRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartProductRepository cartProductRepository;

	// ==================== 바로 주문 ====================

	@Nested
	@DisplayName("바로 주문 생성 API")
	class 바로_주문_생성_API {

		@Test
		@DisplayName("성공: 상품 페이지에서 바로 주문")
		void 성공() throws Exception {
			// given: 상품과 배송지 준비
			Product product = createProduct("강아지 사료", "맛있는 사료", 15000, PetType.DOG);
			DeliveryAddress address = createDeliveryAddress(DEFAULT_USER_ID);

			OrderRequest.DirectOrder request = new OrderRequest.DirectOrder(
				product.getId(),
				2,  // 수량
				"홍길동",
				"서울시 강남구 테헤란로 123",
				"010-1234-5678",
				address.getId(),
				3000,  // 배송비
				"CARD"
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/direct",
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.orderNumber").exists())
				.andExpect(jsonPath("$.data.orderStatusDescription").value("결제 대기중"))
				.andExpect(jsonPath("$.data.message").value("바로 주문이 생성되었습니다."))
				.andDo(
					restDocsFactory.success(
						"order-create-direct",
						"바로 주문 생성",
						"상품 페이지에서 바로 주문하는 API",
						"Order",
						request,
						null
					)
				);
		}
	}

	// ==================== 장바구니 주문 ====================

	@Nested
	@DisplayName("장바구니 주문 생성 API")
	class 장바구니_주문_생성_API {

		@Test
		@DisplayName("성공: 장바구니 상품들을 주문")
		void 성공() throws Exception {
			// given: 장바구니에 상품 담기
			Product product = createProduct("고양이 간식", "맛있는 간식", 8000, PetType.CAT);
			Cart cart = createCart(DEFAULT_USER_ID);
			createCartProduct(cart, product, 3);

			DeliveryAddress address = createDeliveryAddress(DEFAULT_USER_ID);

			OrderRequest.CartOrder request = new OrderRequest.CartOrder(
				"김철수",
				"서울시 서초구 반포대로 456",
				"010-9876-5432",
				address.getId(),
				2500,  // 배송비
				"CARD"
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/cart",
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.orderNumber").exists())
				.andExpect(jsonPath("$.data.orderStatusDescription").value("결제 대기중"))
				.andExpect(jsonPath("$.data.message").value("장바구니 상품으로 주문이 생성되었습니다."))
				.andDo(
					restDocsFactory.success(
						"order-create-cart",
						"장바구니 주문 생성",
						"장바구니에 담긴 상품들을 주문하는 API",
						"Order",
						request,
						null
					)
				);
		}
	}

	// ==================== 주문 조회 ====================

	@Nested
	@DisplayName("주문 목록 조회 API")
	class 주문_목록_조회_API {

		@Test
		@DisplayName("성공: 내 주문 목록 조회")
		void 성공() throws Exception {
			// given
			createTestOrderWithPayment(DEFAULT_USER_ID, "ORD-TEST-001");
			createTestOrderWithPayment(DEFAULT_USER_ID, "ORD-TEST-002");

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
				.andDo(
					restDocsFactory.success(
						"order-list",
						"주문 목록 조회",
						"로그인한 사용자의 주문 목록을 조회하는 API",
						"Order",
						null,
						null
					)
				);
		}
	}

	@Nested
	@DisplayName("주문 상세 조회 API")
	class 주문_상세_조회_API {

		@Test
		@DisplayName("성공: 주문 상세 정보 조회")
		void 성공() throws Exception {
			// given
			Order order = createTestOrderWithPayment(DEFAULT_USER_ID, "ORD-DETAIL-001");

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{orderNumber}",
							null,
							HttpMethod.GET,
							objectMapper,
							order.getOrderNumber()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.orderNumber").value(order.getOrderNumber()))
				.andDo(
					restDocsFactory.success(
						"order-detail",
						"주문 상세 조회",
						"특정 주문의 상세 정보를 조회하는 API",
						"Order",
						null,
						null
					)
				);
		}
	}

	// ==================== 주문 취소 ====================

	@Nested
	@DisplayName("주문 취소 API")
	class 주문_취소_API {

		@Test
		@DisplayName("성공: 결제 전 주문 취소")
		void 성공() throws Exception {
			// given: PENDING 상태의 주문
			Order order = createTestOrderWithPayment(DEFAULT_USER_ID, "ORD-CANCEL-001");

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{orderNumber}/cancel",
							null,
							HttpMethod.PATCH,
							objectMapper,
							order.getOrderNumber()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"))
				.andDo(
					restDocsFactory.success(
						"order-cancel",
						"주문 취소",
						"결제 전 주문을 취소하는 API (PENDING 상태에서만 가능)",
						"Order",
						null,
						null
					)
				);
		}
	}

	// ==================== 주문 수정 ====================

	@Nested
	@DisplayName("주문 정보 수정 API")
	class 주문_정보_수정_API {

		@Test
		@DisplayName("성공: 배송 정보 수정")
		void 성공() throws Exception {
			// given
			Order order = createTestOrderWithPayment(DEFAULT_USER_ID, "ORD-UPDATE-001");

			OrderRequest.Update request = new OrderRequest.Update(
				"이영희",
				"부산시 해운대구 우동 789",
				"010-5555-6666"
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{orderNumber}",
							request,
							HttpMethod.PATCH,
							objectMapper,
							order.getOrderNumber()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"order-update",
						"주문 정보 수정",
						"주문의 배송 정보를 수정하는 API (PENDING 상태에서만 가능)",
						"Order",
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

	private DeliveryAddress createDeliveryAddress(Long userId) {
		DeliveryAddress address = new DeliveryAddress(
			userId,
			"홍길동",
			"집",
			"010-1234-5678",
			"12345",
			"서울시 강남구",
			"테헤란로 123",
			true,
			true
		);
		return deliveryAddressRepository.save(address);
	}

	private Cart createCart(Long userId) {
		Cart cart = Cart.create(userId);
		return cartRepository.save(cart);
	}

	private CartProduct createCartProduct(Cart cart, Product product, int count) {
		CartProduct cartProduct = CartProduct.create(cart, product, count);
		return cartProductRepository.save(cartProduct);
	}

	private Order createTestOrderWithPayment(Long userId, String orderNumber) {
		Receiver receiver = new Receiver("테스트수령인", "서울시 강남구", "010-1234-5678");
		Order order = Order.create(userId, receiver, orderNumber, OrderType.DIRECT);
		orderRepository.save(order);

		// Payment 생성 (양방향 관계 자동 설정됨)
		Payment payment = Payment.create(userId, order, 3000L, PaymentType.CARD);
		paymentRepository.save(payment);

		return order;
	}
}