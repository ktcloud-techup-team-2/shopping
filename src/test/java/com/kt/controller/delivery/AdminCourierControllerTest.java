package com.kt.controller.delivery;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.delivery.Courier;
import com.kt.dto.delivery.CourierRequest;
import com.kt.dto.delivery.CourierResponse;
import com.kt.repository.delivery.CourierRepository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
class AdminCourierControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/admin/delivery/couriers";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private CourierRepository courierRepository;

	@Nested
	class 택배사_등록_API {
		@Test
		void 성공() throws Exception {
			// given
			var request = new CourierRequest.Create("CJ", "CJ대한통운");

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
				.andDo(result -> {

					var savedCourier = courierRepository.findByCode(request.code()).orElseThrow(
						() -> new CustomException(ErrorCode.COURIER_NOT_FOUND)
					);
					var docsResponse = ApiResponse.of(CourierResponse.from(savedCourier));

					restDocsFactory.success(
						"admin-courier-create",
						"택배사 등록",
						"관리자가 새로운 택배사를 등록합니다.",
						"Admin-Courier",
						request,
						docsResponse
					).handle(result);
					}
				);
		}
	}

	@Nested
	class 택배사_목록_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			Courier courier1 = createCourier("CJ", "CJ대한통운");
			Courier courier2 = createCourier("POST", "우체국");

			List<Courier> courierList = List.of(courier1, courier2);
			var docsResponse = courierList.stream()
				.map(CourierResponse::from)
				.toList();
			var finalDocsResponse = ApiResponse.of(docsResponse);

			// when & then
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
						"admin-courier-list",
						"택배사 목록 조회",
						"전체 택배사 목록을 조회합니다.",
						"Admin-Courier",
						null,
						finalDocsResponse
					)
				);
		}
	}

	@Nested
	class 택배사_수정_API {
		@Test
		void 성공() throws Exception {
			// given
			Courier courier = createCourier("OLD", "수정 전 이름");
			Long courierId = courier.getId();

			var request = new CourierRequest.Update("CJ GLS (수정됨)", false);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{courierId}",
						request,
						HttpMethod.PUT,
						objectMapper,
						courierId
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(result -> {
					var updatedCourier = courierRepository.findById(courierId).orElseThrow();
					var docsResponse = ApiResponse.of(CourierResponse.from(updatedCourier));

					restDocsFactory.success(
						"admin-courier-update",
						"택배사 수정",
						"택배사 명칭 및 사용 여부를 수정합니다.",
						"Admin-Courier",
						request,
						docsResponse
					).handle(result);
				});
		}
	}

	@Nested
	class 택배사_삭제_API {
		@Test
		void 성공() throws Exception {
			// given
			Courier courier = createCourier("DELETE", "삭제 대상");
			Long courierId = courier.getId();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{courierId}",
						null,
						HttpMethod.DELETE,
						objectMapper,
						courierId
					).with(jwtAdmin())
				)
				.andExpect(status().isNoContent())
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

	private Courier createCourier(String code, String name) {
		Courier courier = Courier.create(code, name);
		return courierRepository.save(courier);
	}
}