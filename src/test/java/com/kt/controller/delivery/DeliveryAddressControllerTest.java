package com.kt.controller.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.dto.delivery.DeliveryAddressResponse;
import com.kt.repository.delivery.DeliveryAddressRepository;

@Transactional
class DeliveryAddressControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/delivery/addresses";
	private static final Long TEST_USER_ID = 1L;

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private DeliveryAddressRepository deliveryAddressRepository;

	@Nested
	class 배송지_생성_API {
		@Test
		void 성공() throws Exception {
			// given
			var request = new DeliveryAddressRequest.Create(
				"집",
				"홍길동",
				"010-1234-5678",
				"12345",
				"서울시 강남구",
				"101호",
				true
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL,
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andDo(result -> {
					var response = objectMapper.readValue(result.getResponse().getContentAsString(), ApiResponse.class);
					var responseData = (LinkedHashMap) response.getData();
					var id = Long.valueOf(responseData.get("id").toString());

					var createdAddress = deliveryAddressRepository.findById(id).orElseThrow();
					var docsResponse = ApiResponse.of(DeliveryAddressResponse.from(createdAddress));

					restDocsFactory.success(
						"delivery-address-create",
						"배송지 생성",
						"새로운 배송지를 등록합니다.",
						"Delivery-Address",
						request,
						docsResponse
					).handle(result);
				});
		}
	}

	@Nested
	class 배송지_목록_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			DeliveryAddress addr1 = createAddress("집", true, TEST_USER_ID);
			DeliveryAddress addr2 = createAddress("회사", false, TEST_USER_ID);

			List<DeliveryAddressResponse> realList = List.of(
				DeliveryAddressResponse.from(addr1),
				DeliveryAddressResponse.from(addr2)
			);

			var docsResponse = ApiResponse.of(realList);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
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
	class 배송지_상세_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			DeliveryAddress address = createAddress("상세조회", false, TEST_USER_ID);
			Long addressId = address.getId();

			var docsResponse = ApiResponse.of(DeliveryAddressResponse.from(address));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{addressId}",
							null,
							HttpMethod.GET,
							objectMapper,
							addressId
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"delivery-address-detail",
						"배송지 상세 조회",
						"특정 ID의 배송지 상세 정보를 조회합니다.",
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
			Long addressId = createAddress("수정전", true, TEST_USER_ID).getId();
			var request = new DeliveryAddressRequest.Update(
				"회사",
				"김대리",
				"010-9876-5432",
				"54321",
				"판교로",
				"사옥 1층"
			);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{addressId}",
							request,
							HttpMethod.PUT,
							objectMapper,
							addressId
						)
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(result -> {
					var updatedAddress = deliveryAddressRepository.findById(addressId).orElseThrow();
					var docsResponse = ApiResponse.of(DeliveryAddressResponse.from(updatedAddress));

					restDocsFactory.success(
						"delivery-address-update",
						"배송지 수정",
						"기존 배송지 정보를 수정합니다.",
						"Delivery-Address",
						request,
						docsResponse
					).handle(result);
				});
		}
	}

	@Nested
	class 기본_배송지_설정_API {
		@Test
		void 성공() throws Exception {
			// given
			DeliveryAddress currentDefault = createAddress("기존_기본", true, TEST_USER_ID);
			DeliveryAddress newDefault = createAddress("새로운_기본", false, TEST_USER_ID);

			Long currentDefaultId = currentDefault.getId();
			Long newDefaultId = newDefault.getId();

			// when
			ResultActions perform = mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{addressId}/set-default",
							null,
							HttpMethod.PATCH,
							objectMapper,
							newDefaultId
						)
						.with(jwtUser())
				)
				.andExpect(status().isNoContent());

			DeliveryAddress oldAddress = deliveryAddressRepository.findById(currentDefaultId).orElseThrow();
			assertThat(oldAddress.getIsDefault()).isFalse();

			DeliveryAddress newAddress = deliveryAddressRepository.findById(newDefaultId).orElseThrow();
			assertThat(newAddress.getIsDefault()).isTrue();

			perform.andDo(
				restDocsFactory.success(
					"delivery-address-set-default",
					"기본 배송지 설정",
					"특정 배송지를 사용자의 기본 배송지로 설정합니다. (기존 기본 배송지는 해제됨)",
					"Delivery-Address",
					null,
					null
				)
			);
		}
	}

	@Nested
	class 배송지_삭제_API {
		@Test
		void 성공() throws Exception {
			// given
			Long addressId = createAddress("삭제대상", true, TEST_USER_ID).getId();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{addressId}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							addressId
						)
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
						null
					)
				);

			DeliveryAddress deleted = deliveryAddressRepository.findById(addressId).orElseThrow();
			assertThat(deleted.getIsActive()).isFalse();
		}
	}

	private DeliveryAddress createAddress(String addressName, boolean isDefault, Long userId) {
		DeliveryAddressRequest.Create request = new DeliveryAddressRequest.Create(
			addressName,
			"홍길동",
			"010-1234-5678",
			"12345",
			"서울시 강남구",
			"101호",
			isDefault
		);
		DeliveryAddress address = DeliveryAddress.from(userId, request);
		return deliveryAddressRepository.save(address);
	}
}