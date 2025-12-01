package com.kt.controller.delivery;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Boot 3.4+

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.dto.delivery.DeliveryAddressResponse;
import com.kt.service.delivery.DeliveryAddressService;

class DeliveryAddressControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/delivery/addresses";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@MockitoBean // or @MockBean
	private DeliveryAddressService deliveryAddressService;

	@Nested
	class 배송지_생성_API {
		@Test
		void 성공() throws Exception {
			// given
			Long userId = 1L; // AbstractRestDocsTest의 jwtUser()가 1L로 세팅됨
			var request = new DeliveryAddressRequest.Create(
				"집", "홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", true
			);

			var realResponse = createMockResponse(100L, "집", true);

			// Service Mocking
			given(deliveryAddressService.createAddress(eq(userId), any(DeliveryAddressRequest.Create.class)))
				.willReturn(realResponse);

			// Shadow DTO (StackOverflow 방지)
			var docsResponse = TestDeliveryAddressResponse.from(realResponse);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL,
							request,
							HttpMethod.POST,
							objectMapper
						)
						.param("userId", String.valueOf(userId)) // 파라미터 추가
						.with(jwtUser()) // 인증 토큰 주입 (csrf는 AbstractRestDocsTest 설정에 따라 다를 수 있으나 보통 통합 테스트엔 포함됨)
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"delivery-address-create",
						"배송지 생성",
						"새로운 배송지를 등록합니다.",
						"Delivery-Address",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 배송지_목록_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			Long userId = 1L;
			var realList = List.of(
				createMockResponse(100L, "집", true),
				createMockResponse(101L, "회사", false)
			);

			given(deliveryAddressService.getAddressList(userId)).willReturn(realList);

			// Shadow DTO List 변환
			var docsResponse = realList.stream().map(TestDeliveryAddressResponse::from).toList();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						.param("userId", String.valueOf(userId))
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"delivery-address-list",
						"배송지 목록 조회",
						"유저의 모든 배송지를 조회합니다.",
						"Delivery-Address",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 배송지_수정_API {
		@Test
		void 성공() throws Exception {
			// given
			Long userId = 1L;
			Long addressId = 100L;
			var request = new DeliveryAddressRequest.Update(
				"회사", "김대리", "010-9876-5432", "54321", "판교", "사옥"
			);

			var realResponse = createMockResponse(addressId, "회사", true);

			given(deliveryAddressService.updateAddress(eq(userId), eq(addressId), any(DeliveryAddressRequest.Update.class)))
				.willReturn(realResponse);

			var docsResponse = TestDeliveryAddressResponse.from(realResponse);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{addressId}",
							request,
							HttpMethod.PUT,
							objectMapper,
							addressId
						)
						.param("userId", String.valueOf(userId))
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"delivery-address-update",
						"배송지 수정",
						"기존 배송지 정보를 수정합니다.",
						"Delivery-Address",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 배송지_삭제_API {
		@Test
		void 성공() throws Exception {
			// given
			Long userId = 1L;
			Long addressId = 100L;

			willDoNothing().given(deliveryAddressService).deleteAddress(userId, addressId);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{addressId}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							addressId
						)
						.param("userId", String.valueOf(userId))
						.with(jwtUser())
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"delivery-address-delete",
						"배송지 삭제",
						"배송지를 삭제(비활성화) 처리합니다.",
						"Delivery-Address",
						null,
						null // 응답 바디 없음
					)
				);
		}
	}

	// --- Helper Methods & Shadow DTO ---

	private DeliveryAddressResponse createMockResponse(Long id, String name, boolean isDefault) {
		return new DeliveryAddressResponse(
			id, name, "홍길동", "010-1234-5678",
			"12345", "서울시 강남구", "101호",
			isDefault, true,
			LocalDateTime.now(), LocalDateTime.now()
		);
	}

	// 문서화용 Shadow DTO (LocalDateTime -> String)
	static class TestDeliveryAddressResponse {
		Long id;
		String addressName;
		String receiverName;
		String receiverMobile;
		String postalCode;
		String roadAddress;
		String detailAddress;
		Boolean isDefault;
		Boolean isActive;
		String createdAt;
		String updatedAt;

		static TestDeliveryAddressResponse from(DeliveryAddressResponse real) {
			var dto = new TestDeliveryAddressResponse();
			dto.id = real.id();
			dto.addressName = real.addressName();
			dto.receiverName = real.receiverName();
			dto.receiverMobile = real.receiverMobile();
			dto.postalCode = real.postalCode();
			dto.roadAddress = real.roadAddress();
			dto.detailAddress = real.detailAddress();
			dto.isDefault = real.isDefault();
			dto.isActive = real.isActive();
			dto.createdAt = real.createdAt().toString();
			dto.updatedAt = real.updatedAt().toString();
			return dto;
		}
	}
}