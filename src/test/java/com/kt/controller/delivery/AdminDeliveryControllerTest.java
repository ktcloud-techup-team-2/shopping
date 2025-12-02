package com.kt.controller.delivery;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.service.delivery.DeliveryService;

class AdminDeliveryControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/admin/delivery/orders";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@MockitoBean
	private DeliveryService deliveryService;

	@Nested
	@DisplayName("관리자 배송 목록 조회 API")
	class GetDeliveryList {
		@Test
		void 성공() throws Exception {
			// given
			var realSimple = new DeliveryResponse.Simple(
				100L, 1L, "TRACK123", "CJ",
				DeliveryStatus.PENDING, LocalDateTime.now()
			);
			Page<DeliveryResponse.Simple> page = new PageImpl<>(List.of(realSimple));

			// Service Mocking
			given(deliveryService.getDeliveryList(any(Pageable.class))).willReturn(page);

			// Shadow DTO List 변환 (ApiResponse 포장)
			// *참고: 실제 응답은 PageBlock 정보도 포함되지만, RestDocs 문서화는 핵심 데이터(content) 위주로 작성
			var shadowList = List.of(TestDeliverySimple.from(realSimple));
			var docsResponse = TestPageResponse.of(shadowList);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL,
						null,
						HttpMethod.GET,
						objectMapper
					).with(jwtAdmin()) // 관리자 권한 필수
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-delivery-list",
						"배송 목록 조회",
						"관리자가 배송 전체 목록을 페이징하여 조회합니다.",
						"Admin-Delivery",
						null,
						docsResponse // 가짜 DTO(String Date) 전달
					)
				);
		}
	}

	@Nested
	@DisplayName("관리자 배송 상세 조회 API")
	class GetDeliveryDetail {
		@Test
		void 성공() throws Exception {
			// given
			Long deliveryId = 100L;
			var realResponse = createDetailResponse(deliveryId, DeliveryStatus.PENDING);

			given(deliveryService.getDeliveryDetail(deliveryId)).willReturn(realResponse);

			// Shadow DTO
			var docsResponse = ApiResponse.of(TestDeliveryDetail.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL + "/{deliveryId}",
						null,
						HttpMethod.GET,
						objectMapper,
						deliveryId
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-delivery-detail",
						"배송 상세 조회",
						"관리자가 특정 배송 건의 상세 정보를 조회합니다.",
						"Admin-Delivery",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("관리자 배송 상태 변경 API")
	class UpdateDeliveryStatus {
		@Test
		void 성공() throws Exception {
			// given
			Long deliveryId = 100L;
			// 상태 변경 요청 (배송중으로 변경)
			var request = new DeliveryRequest.UpdateStatus(
				DeliveryStatus.SHIPPING, "CJ", "TRACK999"
			);

			// 변경된 결과 응답 Mock
			var realResponse = createDetailResponse(deliveryId, DeliveryStatus.SHIPPING);

			given(deliveryService.updateDeliveryStatus(eq(deliveryId), any(DeliveryRequest.UpdateStatus.class)))
				.willReturn(realResponse);

			// Shadow DTO
			var docsResponse = ApiResponse.of(TestDeliveryDetail.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL + "/{deliveryId}/status",
						request,
						HttpMethod.PATCH,
						objectMapper,
						deliveryId
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-delivery-update-status",
						"배송 상태 변경",
						"관리자가 배송 상태를 변경합니다. (출고 시 송장정보 필수)",
						"Admin-Delivery",
						request,
						docsResponse
					)
				);
		}
	}

	// --- Helper Methods & Shadow DTOs ---

	private DeliveryResponse.Detail createDetailResponse(Long deliveryId, DeliveryStatus status) {
		return new DeliveryResponse.Detail(
			deliveryId, 1L, "홍길동", "010-1234-5678",
			"12345", "서울", "상세", 3000,
			status, "TRACK123", "CJ", LocalDateTime.now()
		);
	}

	// 1. 목록 조회용 Shadow DTO
	static class TestDeliverySimple {
		Long deliveryId;
		Long orderId;
		String trackingNumber;
		String courierCode;
		DeliveryStatus status;
		String createdAt;

		static TestDeliverySimple from(DeliveryResponse.Simple real) {
			var dto = new TestDeliverySimple();
			dto.deliveryId = real.deliveryId();
			dto.orderId = real.orderId();
			dto.trackingNumber = real.trackingNumber();
			dto.courierCode = real.courierCode();
			dto.status = real.status();
			dto.createdAt = real.createdAt().toString();
			return dto;
		}
	}

	static class TestPageResponse<T> {
		List<T> data;
		TestPageBlock page; // PageBlock 구조와 일치해야 함

		static <T> TestPageResponse<T> of(List<T> data) {
			TestPageResponse<T> response = new TestPageResponse<>();
			response.data = data;
			response.page = new TestPageBlock(0, 1, 1L, 1, false, false, List.of());
			return response;
		}
	}
	record TestPageBlock(
		int number,
		int size,
		long totalElements,
		int totalPages,
		boolean hasNext,
		boolean hasPrev,
		List<String> sort // SortOrder 대신 간단히 처리 (필요시 상세 구현)
	) {}



	// 2. 상세/수정 응답용 Shadow DTO (courierCode 포함 확인!)
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
		String courierCode; // ✅ 필드 확인
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
}