package com.kt.controller.delivery;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.service.delivery.DeliveryService;

class DeliveryControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/delivery";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@MockitoBean // @MockBean (버전에 따라 선택)
	private DeliveryService deliveryService;

	@Nested
	@DisplayName("배송 생성 API")
	class CreateDelivery {
		@Test
		void 성공() throws Exception {
			// given
			Long orderId = 1L;
			var request = new DeliveryRequest.Create(orderId, 100L, 3000);

			// 실제 서비스 반환값 (LocalDateTime 포함)
			var realResponse = createDetailResponse(orderId);

			given(deliveryService.createDelivery(any(DeliveryRequest.Create.class)))
				.willReturn(realResponse);

			// 문서화용 Shadow DTO (LocalDateTime -> String 변환 + ApiResponse 포장)
			var docsResponse = ApiResponse.of(TestDeliveryDetail.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser()) // 인증 토큰 (시스템 내부 호출이라도 보안 통과용)
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"delivery-create",
						"배송 생성",
						"주문 건에 대한 배송 정보를 생성합니다. (시스템 내부용)",
						"Delivery",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("배송 상태 조회 API")
	class GetDeliveryStatus {
		@Test
		void 성공() throws Exception {
			// given
			Long orderId = 1L;
			var realResponse = createDetailResponse(orderId);

			given(deliveryService.getDeliveryByOrderId(orderId)).willReturn(realResponse);

			var docsResponse = ApiResponse.of(TestDeliveryDetail.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL + "/orders/{orderId}/status",
						null,
						HttpMethod.GET,
						objectMapper,
						orderId
					).with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"delivery-get-status",
						"배송 상태 조회",
						"주문 번호로 배송 상태를 조회합니다.",
						"Delivery",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("배송 추적 API")
	class TrackDelivery {
		@Test
		void 성공() throws Exception {
			// given
			String trackingNumber = "TRACK123456";
			var realResponse = new DeliveryResponse.Tracking(
				100L, "CJ", trackingNumber,
				DeliveryStatus.SHIPPING, LocalDateTime.now(), null
			);

			given(deliveryService.trackDelivery(trackingNumber)).willReturn(realResponse);

			var docsResponse = ApiResponse.of(TestDeliveryTracking.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL + "/tracking/{trackingNumber}",
						null,
						HttpMethod.GET,
						objectMapper,
						trackingNumber
					).with(jwtUser()) // 비회원 허용 시 .with(jwtUser()) 생략 가능하나, 테스트 통과를 위해 넣음
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"delivery-track",
						"배송 추적",
						"송장 번호로 배송을 추적합니다.",
						"Delivery",
						null,
						docsResponse
					)
				);
		}
	}

	// --- Helper Method ---
	private DeliveryResponse.Detail createDetailResponse(Long orderId) {
		return new DeliveryResponse.Detail(
			100L, orderId, "홍길동", "010-1234-5678",
			"12345", "서울", "상세", 3000,
			DeliveryStatus.PENDING, "TRACK123", "CJ", LocalDateTime.now()
		);
	}

	// --- Shadow DTOs (문서화용) ---

	// 1. Detail용 Shadow DTO
	static class TestDeliveryDetail {
		Long deliveryId;
		Long orderId;
		String receiverName;
		String receiverMobile;
		String postalCode;
		String roadAddress;
		String detailAddress;
		Integer deliveryFee;
		DeliveryStatus status;
		String trackingNumber;
		String courierCode;
		String createdAt;

		static TestDeliveryDetail from(DeliveryResponse.Detail real) {
			var dto = new TestDeliveryDetail();
			dto.deliveryId = real.deliveryId();
			dto.orderId = real.orderId();
			dto.receiverName = real.receiverName();
			dto.receiverMobile = real.receiverMobile();
			dto.postalCode = real.postalCode();
			dto.roadAddress = real.roadAddress();
			dto.detailAddress = real.detailAddress();
			dto.deliveryFee = real.deliveryFee();
			dto.status = real.status();
			dto.trackingNumber = real.trackingNumber();
			dto.courierCode = real.courierCode();
			dto.createdAt = real.createdAt().toString();
			return dto;
		}
	}

	// 2. Tracking용 Shadow DTO
	static class TestDeliveryTracking {
		Long deliveryId;
		String courierCode;
		String trackingNumber;
		DeliveryStatus status;
		String shippedAt;
		String deliveredAt;

		static TestDeliveryTracking from(DeliveryResponse.Tracking real) {
			var dto = new TestDeliveryTracking();
			dto.deliveryId = real.deliveryId();
			dto.courierCode = real.courierCode();
			dto.trackingNumber = real.trackingNumber();
			dto.status = real.status();
			dto.shippedAt = real.shippedAt() != null ? real.shippedAt().toString() : null;
			dto.deliveredAt = real.deliveredAt() != null ? real.deliveredAt().toString() : null;
			return dto;
		}
	}
}