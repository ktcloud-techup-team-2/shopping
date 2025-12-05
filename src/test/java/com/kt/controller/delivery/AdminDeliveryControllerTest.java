package com.kt.controller.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.delivery.Courier;
import com.kt.domain.delivery.Delivery;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.repository.delivery.CourierRepository;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.delivery.DeliveryRepository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Transactional
class AdminDeliveryControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/admin/delivery/orders";
	private static final Long TEST_USER_ID = 1L;

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private DeliveryRepository deliveryRepository;

	@Autowired
	private DeliveryAddressRepository deliveryAddressRepository;

	@Autowired
	private CourierRepository courierRepository;

	@Nested
	class 관리자_배송_목록_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			IntStream.range(1, 12).forEach(i -> createDeliveryForList(
				(long)i,
				"주문자" + i,
				(long)i
			));

			PageRequest pageable = PageRequest.of(0, 10);

			// when & then
			mockMvc.perform(
					restDocsFactory.createParamRequest(
						DEFAULT_URL,
						null,
						pageable,
						objectMapper
					).with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(result -> {
						var page = deliveryRepository.findAll(pageable);
						var docsResponseContent = page.getContent().stream()
							.map(DeliveryResponse.Simple::from)
							.toList();

						var docsResponse = ApiResponse.ofPage(docsResponseContent, toPageBlock(page));

						restDocsFactory.success(
							"admin-delivery-list",
							"배송 목록 조회",
							"관리자가 배송 전체 목록을 페이징하여 조회합니다.",
							"Admin-Delivery",
							null,
							docsResponse
						).handle(result);
					}
				);
		}
	}

	@Nested
	class 관리자_배송_상세_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			Delivery delivery = createDelivery(1L, "홍길동", 100L);
			DeliveryAddress address = deliveryAddressRepository.findById(delivery.getDeliveryAddressId()).orElseThrow();
			Long deliveryId = delivery.getId();

			var docsResponse = ApiResponse.of(DeliveryResponse.Detail.from(delivery, address));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{deliveryId}",
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
	class 관리자_배송_상태_변경_API {
		@Test
		void 성공_배송_상태_변경_출고() throws Exception {
			// given
			createCourier("CJ", "CJ대한통운");

			Delivery delivery = createDelivery(1L, "홍길동", 100L);
			Long deliveryId = delivery.getId();
			DeliveryAddress address = deliveryAddressRepository.findById(delivery.getDeliveryAddressId()).orElseThrow();

			updateStatusApi(deliveryId, DeliveryStatus.PREPARING, null, null)
				.andExpect(status().isOk());

			updateStatusApi(deliveryId, DeliveryStatus.READY, null, null)
				.andExpect(status().isOk());

			var request = new DeliveryRequest.UpdateStatus(
				DeliveryStatus.SHIPPING, "CJ", "TRACK999"
			);

			// when & then
			ResultActions perform = mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/{deliveryId}/status",
						request,
						HttpMethod.PATCH,
						objectMapper,
						deliveryId
					).with(jwtAdmin())
				)
				.andExpect(status().isOk());

			perform.andDo(result -> {
				var updatedDelivery = deliveryRepository.findById(deliveryId).orElseThrow();
				var docsResponse = ApiResponse.of(DeliveryResponse.Detail.from(updatedDelivery, address));

				restDocsFactory.success(
					"admin-delivery-update-status",
					"배송 상태 변경",
					"관리자가 배송 상태를 변경합니다. (출고 시 송장정보 필수)",
					"Admin-Delivery",
					request,
					docsResponse
				).handle(result);
			});
		}

	}

	@Nested
	class 관리자_송장번호_등록_API {

		@Test
		void 성공() throws Exception {
			// given
			createCourier("CJ", "CJ대한통운");

			Delivery delivery = createDelivery(10L, "홍길동", 1000L);
			Long deliveryId = delivery.getId();
			DeliveryAddress address = deliveryAddressRepository.findById(delivery.getDeliveryAddressId()).orElseThrow();

			updateStatusApi(deliveryId, DeliveryStatus.PREPARING, null, null).andExpect(status().isOk());
			updateStatusApi(deliveryId, DeliveryStatus.READY, null, null).andExpect(status().isOk());

			var request = new DeliveryRequest.RegisterTracking(
				"CJ",
				"TRACK123456"
			);

			ResultActions perform = registerTrackingApi(deliveryId, request)
				.andExpect(status().isOk());

			// then
			perform.andDo(result -> {
				var updatedDelivery = deliveryRepository.findById(deliveryId).orElseThrow();
				var docsResponse = ApiResponse.of(DeliveryResponse.Detail.from(updatedDelivery, address));

				assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.SHIPPING);
				assertThat(updatedDelivery.getTrackingNumber()).isEqualTo("TRACK123456");
				assertThat(updatedDelivery.getCourierCode()).isEqualTo("CJ");

				restDocsFactory.success(
					"admin-delivery-register-tracking-number",
					"송장 번호 등록",
					"관리자가 배송 건에 대해 송장 번호를 등록하고 상태를 SHIPPING으로 변경합니다. (상태는 READY여야 성공)",
					"Admin-Delivery",
					request,
					docsResponse
				).handle(result);
			});
		}
	}

	private ResultActions registerTrackingApi(Long deliveryId, DeliveryRequest.RegisterTracking request) throws Exception {
		return mockMvc.perform(
			restDocsFactory.createRequest(
				DEFAULT_URL + "/{deliveryId}/tracking-number",
				request,
				HttpMethod.POST,
				objectMapper,
				deliveryId
			).with(jwtAdmin())
		);
	}

	private ResultActions updateStatusApi(Long deliveryId, DeliveryStatus status, String courierCode,
		String trackingNumber) throws Exception {
		var statusRequest = new DeliveryRequest.UpdateStatus(status, courierCode, trackingNumber);
		return mockMvc.perform(
			restDocsFactory.createRequest(
				DEFAULT_URL + "/{deliveryId}/status",
				statusRequest,
				HttpMethod.PATCH,
				objectMapper,
				deliveryId
			).with(jwtAdmin())
		);
	}

	private Delivery createDeliveryForList(Long orderId, String receiverName, Long deliveryAddressIdSeed) {
		DeliveryAddress address = createDeliveryAddress(receiverName + deliveryAddressIdSeed);
		Delivery delivery = Delivery.create(orderId, address.getId(), 3000);
		return deliveryRepository.save(delivery);
	}

	private Delivery createDelivery(Long orderId, String receiverName, Long deliveryAddressIdSeed) {
		DeliveryAddress address = createDeliveryAddress(receiverName + deliveryAddressIdSeed);
		Delivery delivery = Delivery.create(
			orderId,
			address.getId(),
			3000
		);

		return deliveryRepository.save(delivery);
	}

	private DeliveryAddress createDeliveryAddress(String receiverName) {
		DeliveryAddress address = new DeliveryAddress(
			TEST_USER_ID,
			"기본 배송지",
			receiverName,
			"010-1111-2222",
			"12345",
			"서울 강남로 100",
			"101호",
			true,
			true
		);

		return deliveryAddressRepository.save(address);
	}

	private Courier createCourier(String code, String name) {
		Courier courier = Courier.create(code, name);
		return courierRepository.save(courier);
	}

	private PageBlock toPageBlock(Page<?> page) {
		return new PageBlock(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext(),
			page.hasPrevious(),
			page.getSort().stream()
				.map(order -> new PageBlock.SortOrder(order.getProperty(), order.getDirection().name()))
				.toList()
		);
	}
}