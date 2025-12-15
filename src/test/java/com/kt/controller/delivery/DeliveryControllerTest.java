package com.kt.controller.delivery;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.delivery.Delivery;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.delivery.DeliveryRepository;
import com.kt.service.delivery.DeliveryService;

@Transactional
class DeliveryControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/delivery";
	private static final Long TEST_USER_ID = 1L;

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@MockitoBean
	private RedissonClient redissonClient;

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private DeliveryService deliveryService;

	@Autowired
	private DeliveryRepository deliveryRepository;

	@Autowired
	private DeliveryAddressRepository deliveryAddressRepository;

	@Nested
	class 배송_생성_API {
		@Test
		void 성공() throws Exception {
			// given
			Long orderId = 1L;
			DeliveryAddress address = createAddress(TEST_USER_ID, "테스트 배송지");
			var request = new DeliveryRequest.Create(orderId, address.getId(), 3000);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andDo(result -> {
					var response = objectMapper.readValue(result.getResponse().getContentAsString(), ApiResponse.class);
					var responseData = (LinkedHashMap) response.getData();
					Long deliveryId = Long.valueOf(responseData.get("deliveryId").toString());

					Delivery createdDelivery = deliveryRepository.findById(deliveryId).orElseThrow();
					DeliveryAddress createdAddress = deliveryAddressRepository.findById(createdDelivery.getDeliveryAddressId()).orElseThrow();

					var docsResponse = ApiResponse.of(DeliveryResponse.Detail.from(createdDelivery, createdAddress));

					restDocsFactory.success(
						"delivery-create",
						"배송 생성",
						"주문 건에 대한 배송 정보를 생성합니다. (시스템 내부용)",
						"Delivery",
						request,
						docsResponse
					).handle(result);
				});
		}
	}

	@Nested
	class 배송_상태_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			Long orderId = 1L;
			Delivery delivery = createDeliveryWithHistory(orderId, TEST_USER_ID, "조회용 주소");
			DeliveryAddress address = deliveryAddressRepository.findById(delivery.getDeliveryAddressId()).orElseThrow();

			var docsResponse = ApiResponse.of(DeliveryResponse.Detail.from(delivery, address));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/orders/{orderId}/status",
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
	class 배송_추적_API {
		@Test
		void 성공() throws Exception {
			// given
			String trackingNumber = "TRACK123456";

			Delivery delivery = createDeliveryWithHistory(99L, TEST_USER_ID, "추적용 주소");

			delivery.startPreparing();
			delivery.readyForShipment();

			delivery.updateTrackingInfo("CJ", trackingNumber);
			delivery.ship();
			deliveryRepository.saveAndFlush(delivery);

			var docsResponse = ApiResponse.of(DeliveryResponse.Tracking.from(delivery));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/tracking/{trackingNumber}",
						null,
						HttpMethod.GET,
						objectMapper,
						trackingNumber
					).with(jwtUser())
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

	private Delivery createDeliveryWithHistory(Long orderId, Long userId, String addressName) {
		DeliveryAddress address = createAddress(userId, addressName);
		DeliveryRequest.Create createRequest = new DeliveryRequest.Create(
			orderId,
			address.getId(),
			3000
		);
		DeliveryResponse.Detail detail = deliveryService.createDelivery(createRequest);

		return deliveryRepository.findById(detail.deliveryId()).orElseThrow();
	}

	private DeliveryAddress createAddress(Long userId, String addressName) {
		DeliveryAddressRequest.Create request = new DeliveryAddressRequest.Create(
			addressName,
			"홍길동",
			"010-1234-5678",
			"12345",
			"서울시 강남구",
			"101호",
			true
		);
		DeliveryAddress address = DeliveryAddress.from(userId, request);
		return deliveryAddressRepository.save(address);
	}
}