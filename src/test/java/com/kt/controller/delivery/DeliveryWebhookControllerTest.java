package com.kt.controller.delivery;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
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
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.dto.delivery.DeliveryStatusWebhookRequest;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.delivery.DeliveryRepository;
import com.kt.service.delivery.DeliveryService;

@Transactional
class DeliveryWebhookControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/webhook/delivery";

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
	class 배송_상태_변경_웹훅_API {
		@Test
		void 성공() throws Exception {
			// given: 배송 데이터 준비 (송장 번호 등록 상태까지 진행)
			String trackingNumber = "WEBHOOK-TRACK-001";
			Delivery delivery = createDeliveryForWebhook(100L, 1L, trackingNumber);

			// 웹훅 요청 DTO 생성 (record 형식 반영)
			var request = new DeliveryStatusWebhookRequest(
				trackingNumber,
				DeliveryStatus.DELIVERED, // 배송 완료로 변경 시도
				LocalDateTime.now()
			);

			var docsResponse = ApiResponse.of(null);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL + "/status",
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"delivery-webhook-status",
						"배송 상태 업데이트 (웹훅)",
						"택배사로부터 배송 상태 변경 알림을 받아 시스템에 반영합니다.",
						"Delivery",
						request,
						docsResponse
					)
				);
			Delivery updatedDelivery = deliveryRepository.findByTrackingNumber(trackingNumber).orElseThrow();
			Assertions.assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
		}
	}

	private Delivery createDeliveryForWebhook(Long orderId, Long userId, String trackingNumber) {
		// 1. 배송지 저장
		DeliveryAddressRequest.Create addressRequest = new DeliveryAddressRequest.Create(
			"웹훅 테스트 주소", "수령인", "010-0000-0000", "12345", "서울", "상세", true
		);
		DeliveryAddress address = deliveryAddressRepository.save(DeliveryAddress.from(userId, addressRequest));

		// 2. 배송 정보 생성 (최초 상태: PENDING)
		DeliveryRequest.Create createRequest = new DeliveryRequest.Create(orderId, address.getId(), 3000);
		DeliveryResponse.Detail detail = deliveryService.createDelivery(createRequest);

		Delivery delivery = deliveryRepository.findById(detail.deliveryId()).orElseThrow();

		// 3. 도메인 규칙에 따른 상태 전환 (에러 방지)
		delivery.startPreparing();  // PENDING -> PREPARING (상품 준비 중)
		delivery.readyForShipment(); // PREPARING -> READY (출고 준비 완료)

		// 4. 송장 등록 및 배송 시작 (READY 상태여야 ship() 가능)
		delivery.updateTrackingInfo("CJ", trackingNumber);
		delivery.ship(); // READY -> SHIPPING

		return deliveryRepository.saveAndFlush(delivery);
	}
}