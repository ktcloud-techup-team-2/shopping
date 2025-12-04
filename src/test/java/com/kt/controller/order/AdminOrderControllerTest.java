package com.kt.controller.order;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.order.Order;
import com.kt.domain.order.OrderStatus;
import com.kt.domain.order.Receiver;
import com.kt.dto.order.OrderRequest;
import com.kt.repository.order.OrderRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AdminOrderControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/admin/orders";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private OrderRepository orderRepository;

	@Nested
	class 관리자_주문_전체_조회_API {

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
						.with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-order-search",
						"관리자 주문 전체 조회",
						"관리자 주문 전체 조회 API",
						"Admin-Order",
						null,
						null
					)
				);
		}
	}

	@Nested
	class 관리자_주문_상세_조회_API {

		@Test
		@DisplayName("성공: 존재하는 주문 상세 조회")
		void 성공() throws Exception {
			Order order = createTestOrder(DEFAULT_USER_ID);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{id}",
							null,
							HttpMethod.GET,
							objectMapper,
							order.getId()
						)
						.with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-order-detail",
						"관리자 주문 상세 조회",
						"관리자 주문 상세 조회 API",
						"Admin-Order",
						null,
						null
					)
				);
		}
	}

	@Nested
	class 관리자_주문_취소_API {

		@Test
		void 성공() throws Exception {
			Order order = createTestOrder(DEFAULT_USER_ID);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{id}/cancel",
							null,
							HttpMethod.DELETE,
							objectMapper,
							order.getId()
						)
						.with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-order-cancel",
						"관리자 주문 취소",
						"관리자 주문 취소 API",
						"Admin-Order",
						null,
						null
					)
				);
		}
	}

	@Nested
	class 관리자_주문_상태_변경_API {

		@Test
		void 성공() throws Exception {
			// given
			Order order = createTestOrder(DEFAULT_USER_ID);
			OrderRequest.ChangeStatus request = new OrderRequest.ChangeStatus(OrderStatus.SHIPPED);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{id}/change-status",
							request,
							HttpMethod.PATCH,
							objectMapper,
							order.getId()
						)
						.with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-order-change",
						"관리자 주문 상태 변경",
						"관리자 주문 상태 변경 API",
						"Admin-Order",
						request,
						null
					)
				);
		}
	}

	private Order createTestOrder(Long userId) {
		Receiver receiver = new Receiver("abc", "서울", "010-0000-0000");
		Order order = Order.create(userId, receiver, 20000L, "ORD-20251202-TESTA");
		return orderRepository.save(order);
	}
}
