package com.kt.controller.delivery;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.dto.delivery.CourierRequest;
import com.kt.dto.delivery.CourierResponse;
import com.kt.service.delivery.CourierService;

class AdminCourierControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/admin/delivery/couriers";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@MockitoBean
	private CourierService courierService;

	@Nested
	@DisplayName("택배사 등록 API")
	class CreateCourier {
		@Test
		void 성공() throws Exception {
			// given
			var request = new CourierRequest.Create("CJ", "CJ대한통운");
			var realResponse = new CourierResponse(1L, "CJ", "CJ대한통운", true);

			given(courierService.createCourier(any())).willReturn(realResponse);

			// Shadow DTO (문서화용)
			var docsResponse = ApiResponse.of(TestCourierResponse.from(realResponse));


			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"admin-courier-create",
						"택배사 등록",
						"관리자가 새로운 택배사를 등록합니다.",
						"Admin-Courier",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("택배사 목록 조회 API")
	class GetCourierList {
		@Test
		void 성공() throws Exception {
			// given
			var list = List.of(
				new CourierResponse(1L, "CJ", "CJ대한통운", true),
				new CourierResponse(2L, "POST", "우체국", true)
			);

			given(courierService.getCourierList()).willReturn(list);

			var docsResponse = list.stream().map(TestCourierResponse::from).toList();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL,
						null,
						HttpMethod.GET,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-courier-list",
						"택배사 목록 조회",
						"전체 택배사 목록을 조회합니다.",
						"Admin-Courier",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("택배사 수정 API")
	class UpdateCourier {
		@Test
		void 성공() throws Exception {
			// given
			Long courierId = 1L;
			var request = new CourierRequest.Update("CJ GLS", false);
			var realResponse = new CourierResponse(1L, "CJ", "CJ GLS", false);

			given(courierService.updateCourier(eq(courierId), any())).willReturn(realResponse);

			var docsResponse = ApiResponse.of(TestCourierResponse.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL + "/{courierId}",
						request,
						HttpMethod.PUT,
						objectMapper,
						courierId
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-courier-update",
						"택배사 수정",
						"택배사 명칭 및 사용 여부를 수정합니다.",
						"Admin-Courier",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("택배사 삭제 API")
	class DeleteCourier {
		@Test
		void 성공() throws Exception {
			// given
			Long courierId = 1L;
			willDoNothing().given(courierService).deleteCourier(courierId);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						BASE_URL + "/{courierId}",
						null,
						HttpMethod.DELETE,
						objectMapper,
						courierId
					).with(jwtAdmin())
				)
				.andExpect(status().isNoContent()) // 204
				.andDo(
					restDocsFactory.success(
						"admin-courier-delete",
						"택배사 삭제",
						"택배사 정보를 삭제합니다.",
						"Admin-Courier",
						null,
						null
					)
				);
		}
	}

	// --- Shadow DTO (RestDocsFactory StackOverflow 방지용) ---
	// CourierResponse에는 날짜 필드가 없지만,
	// 혹시 모를 엔티티 확장(BaseTimeEntity)을 고려해 Shadow DTO 패턴을 유지하는 것이 안전합니다.
	static class TestCourierResponse {
		Long id;
		String code;
		String name;
		Boolean isActive;

		static TestCourierResponse from(CourierResponse real) {
			TestCourierResponse dto = new TestCourierResponse();
			dto.id = real.id();
			dto.code = real.code();
			dto.name = real.name();
			dto.isActive = real.isActive();
			return dto;
		}
	}
}