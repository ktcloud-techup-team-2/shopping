package com.kt.controller.payment;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.order.Order;
import com.kt.domain.order.OrderType;
import com.kt.domain.order.Receiver;
import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentType;
import com.kt.dto.payment.PaymentRequest;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.payment.PaymentRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 컨트롤러 테스트
 * 
 * 테스트 시나리오:
 * 1. 결제 승인 (토스페이먼츠 인증 후)
 * 2. 결제 조회
 * 3. 결제 취소
 */
@Transactional
class PaymentControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/payment";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	// ==================== 결제 승인 ====================

	@Nested
	@DisplayName("결제 승인 API")
	class 결제_승인_API {

		@Test
		@DisplayName("성공: 결제 승인 처리")
		void 성공() throws Exception {
			// given: 주문과 READY 상태의 결제
			Order order = createOrderWithReadyPayment(DEFAULT_USER_ID, "ORD-PAY-001");
			Payment payment = order.getLatestPayment();

			PaymentRequest.Confirm request = new PaymentRequest.Confirm(
				"toss_payment_key_12345",  // 토스에서 받은 결제키
				order.getOrderNumber(),
				payment.getPaymentAmount()
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/confirm",
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("DONE"))
				.andExpect(jsonPath("$.data.paymentKey").value("toss_payment_key_12345"))
				.andDo(
					restDocsFactory.success(
						"payment-confirm",
						"결제 승인",
						"토스페이먼츠 인증 후 최종 결제를 승인하는 API",
						"Payment",
						request,
						null
					)
				);
		}

		@Test
		@DisplayName("실패: 결제 금액 불일치")
		void 결제_금액_불일치() throws Exception {
			// given
			Order order = createOrderWithReadyPayment(DEFAULT_USER_ID, "ORD-PAY-002");

			PaymentRequest.Confirm request = new PaymentRequest.Confirm(
				"toss_payment_key_wrong",
				order.getOrderNumber(),
				999999L  // 잘못된 금액
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/confirm",
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isBadRequest());
		}
	}

	// ==================== 결제 조회 ====================

	@Nested
	@DisplayName("결제 조회 API")
	class 결제_조회_API {

		@Test
		@DisplayName("성공: 결제 상세 정보 조회")
		void 성공() throws Exception {
			// given
			Order order = createOrderWithReadyPayment(DEFAULT_USER_ID, "ORD-PAY-003");
			Payment payment = order.getLatestPayment();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{paymentId}",
							null,
							HttpMethod.GET,
							objectMapper,
							payment.getId()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.orderNumber").value(order.getOrderNumber()))
				.andExpect(jsonPath("$.data.status").value("READY"))
				.andDo(
					restDocsFactory.success(
						"payment-detail",
						"결제 조회",
						"결제 상세 정보를 조회하는 API",
						"Payment",
						null,
						null
					)
				);
		}
	}

	// ==================== 결제 취소 ====================

	@Nested
	@DisplayName("결제 취소 API")
	class 결제_취소_API {

		@Test
		@DisplayName("성공: 결제 완료 후 취소")
		void 성공() throws Exception {
			// given: DONE 상태의 결제
			Order order = createOrderWithDonePayment(DEFAULT_USER_ID, "ORD-PAY-004");
			Payment payment = order.getLatestPayment();

			PaymentRequest.Cancel request = new PaymentRequest.Cancel(
				"고객 요청으로 환불"
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{paymentId}/cancel",
							request,
							HttpMethod.PATCH,
							objectMapper,
							payment.getId()
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("CANCELED"))
				.andDo(
					restDocsFactory.success(
						"payment-cancel",
						"결제 취소",
						"결제 완료된 건을 취소하는 API (DONE 상태에서만 가능)",
						"Payment",
						request,
						null
					)
				);
		}

		@Test
		@DisplayName("실패: READY 상태에서 취소 시도")
		void READY_상태_취소_실패() throws Exception {
			// given: READY 상태의 결제 (아직 승인 안 됨)
			Order order = createOrderWithReadyPayment(DEFAULT_USER_ID, "ORD-PAY-005");
			Payment payment = order.getLatestPayment();

			PaymentRequest.Cancel request = new PaymentRequest.Cancel(
				"취소 사유"
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{paymentId}/cancel",
							request,
							HttpMethod.PATCH,
							objectMapper,
							payment.getId()
						)
						.with(jwtUser())
				)
				.andExpect(status().isBadRequest());
		}
	}

	// ==================== 헬퍼 메서드 ====================

	/**
	 * READY 상태의 Payment를 가진 Order 생성
	 */
	private Order createOrderWithReadyPayment(Long userId, String orderNumber) {
		Receiver receiver = new Receiver("테스트수령인", "서울시 강남구", "010-1234-5678");
		Order order = Order.create(userId, receiver, orderNumber, OrderType.DIRECT);
		orderRepository.save(order);

		// Payment 생성 (기본 READY 상태)
		Payment payment = Payment.create(userId, order, 3000L, PaymentType.CARD);
		paymentRepository.save(payment);

		return order;
	}

	/**
	 * DONE 상태의 Payment를 가진 Order 생성
	 */
	private Order createOrderWithDonePayment(Long userId, String orderNumber) {
		Receiver receiver = new Receiver("테스트수령인", "서울시 강남구", "010-1234-5678");
		Order order = Order.create(userId, receiver, orderNumber, OrderType.DIRECT);
		orderRepository.save(order);

		// Payment 생성 후 승인 처리
		Payment payment = Payment.create(userId, order, 3000L, PaymentType.CARD);
		payment.confirmPayment("test_payment_key_done");  // DONE 상태로 변경
		paymentRepository.save(payment);

		return order;
	}
}
