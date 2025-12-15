package com.kt.controller.order;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.order.Order;
import com.kt.domain.order.Receiver;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.dto.order.OrderRequest;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.product.ProductRepository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OrderControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/orders";

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
	private DeliveryAddressRepository deliveryAddressRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Nested
	class 사용자_주문_전체_조회_API {

		@Test
		void 성공() throws Exception {
			createTestOrder(DEFAULT_USER_ID);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						.param("userId", String.valueOf(DEFAULT_USER_ID))
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"order-search-all",
						"사용자 주문 전체 조회",
						"사용자 주문 전체 조회 API",
						"Order",
						null,
						null
					)
				);
		}
	}

	@Nested
	class 사용자_주문_상세_조회_API {

		@Test
		void 성공() throws Exception {
			Order order = createTestOrder(DEFAULT_USER_ID);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{orderNumber}",
							null,
							HttpMethod.GET,
							objectMapper,
							order.getOrderNumber()
						)
						.param("userId", String.valueOf(DEFAULT_USER_ID))
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"order-detail",
						"사용자 주문 상세 조회",
						"사용자 주문 상세 조회 API",
						"Order",
						null,
						null
					)
				);
		}
	}

	@Nested
	class 사용자_주문_생성_API {

		@Test
		void 성공() throws Exception {
			// given
			Product product = createProduct("테스트 상품", "테스트", 10000, PetType.DOG);
			DeliveryAddress address = createDeliveryAddress(DEFAULT_USER_ID);

			OrderRequest.Create request = new OrderRequest.Create(
				product.getId(),
				"abc",
				"서울",
				"010-0000-0000",
				2,
				address.getId(),
				3000
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							request,
							HttpMethod.POST,
							objectMapper
						)
						.param("userId", String.valueOf(DEFAULT_USER_ID))
						.with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"order-create",
						"사용자 주문 생성",
						"사용자 주문 생성 API",
						"Order",
						request,
						null
					)
				);
		}
	}

	@Nested
	class 사용자_주문_취소_API {

		@Test
		void 성공() throws Exception {
			// given
			Order order = createTestOrder(DEFAULT_USER_ID);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{orderNumber}/cancel",
							null,
							HttpMethod.PATCH,
							objectMapper,
							order.getOrderNumber()
						)
						.param("userId", String.valueOf(DEFAULT_USER_ID))
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"order-cancel",
						"사용자 주문 취소",
						"사용자 주문 취소 API",
						"Order",
						null,
						null
					)
				);
		}
	}


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
			"주소",
			"abc",
			"010-0000-0000",
			"12345",
			"서울",
			"강남",
			true,
			true
		);
		return deliveryAddressRepository.save(address);
	}

	private Order createTestOrder(Long userId) {
		Receiver receiver = new Receiver("abc", "서울", "010-0000-0000");
		Order order = Order.create(userId, receiver, 20000L, "ORD-20251202-TESTA");
		return orderRepository.save(order);
	}
}
