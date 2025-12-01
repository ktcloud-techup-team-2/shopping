package com.kt.service.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.delivery.Delivery;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.domain.delivery.DeliveryStatusHistory;
import com.kt.domain.delivery.event.DeliveryStatusEvent;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.repository.delivery.CourierRepository;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.delivery.DeliveryRepository;
import com.kt.repository.delivery.DeliveryStatusHistoryRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

	@InjectMocks
	private DeliveryService deliveryService;

	@Mock
	private DeliveryRepository deliveryRepository;

	@Mock
	private DeliveryAddressRepository deliveryAddressRepository;

	@Mock
	private DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;

	@Mock
	private CourierRepository courierRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	// --- Helper Methods ---
	private DeliveryAddress mockAddress() {
		return new DeliveryAddress(1L, "Home", "User", "010-1234-5678", "12345", "Road", "Detail", false, true);
	}

	private Delivery mockDelivery(Long id, DeliveryStatus status) {
		Delivery delivery = Delivery.create(1L, 100L, 3000);
		ReflectionTestUtils.setField(delivery, "id", id);
		if (status != DeliveryStatus.PENDING) {
			// 테스트를 위해 강제로 상태 변경 (Setter가 없으므로 Reflection 사용)
			ReflectionTestUtils.setField(delivery, "status", status);
		}
		return delivery;
	}

	@Nested
	@DisplayName("배송 생성 (createDelivery)")
	class CreateTest {
		@Test
		@DisplayName("성공: 배송 생성 시 초기 상태(PENDING)로 이력이 저장된다")
		void success() {
			// given
			var request = new DeliveryRequest.Create(1L, 100L, 3000);

			given(deliveryRepository.existsByOrderId(1L)).willReturn(false);
			given(deliveryAddressRepository.findById(100L)).willReturn(Optional.of(mockAddress()));

			// save 호출 시 ID가 세팅된 객체 반환 Mocking
			given(deliveryRepository.save(any(Delivery.class))).willAnswer(inv -> {
				Delivery d = inv.getArgument(0);
				ReflectionTestUtils.setField(d, "id", 500L);
				return d;
			});

			// when
			var response = deliveryService.createDelivery(request);

			// then
			assertThat(response.status()).isEqualTo(DeliveryStatus.PENDING);

			// ✅ 핵심 검증: 생성 시점에도 이력이 저장되었는가?
			verify(deliveryStatusHistoryRepository, times(1)).save(any(DeliveryStatusHistory.class));
		}
	}

	@Nested
	@DisplayName("배송 상태 변경 (updateDeliveryStatus)")
	class UpdateTest {

		@Test
		@DisplayName("성공: 배송 출발(SHIPPING) 시 송장 정보가 있으면 성공하고 이력이 남는다")
		void success_shipping() {
			// given
			Long deliveryId = 500L;
			// PENDING -> PREPARING -> READY 상태가 된 배송 건 준비
			Delivery delivery = mockDelivery(deliveryId, DeliveryStatus.READY);

			var request = new DeliveryRequest.UpdateStatus(
				DeliveryStatus.SHIPPING, "CJ", "TRACK123456"
			);

			given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(delivery));
			given(deliveryAddressRepository.findById(anyLong())).willReturn(Optional.of(mockAddress()));

			given(courierRepository.existsByCode("CJ")).willReturn(true);

			// when
			var response = deliveryService.updateDeliveryStatus(deliveryId, request);

			// then
			assertThat(response.status()).isEqualTo(DeliveryStatus.SHIPPING);
			assertThat(response.trackingNumber()).isEqualTo("TRACK123456");
			assertThat(response.courierCode()).isEqualTo("CJ");

			// ✅ 핵심 검증: 상태 변경 이력이 저장되었는가?
			verify(deliveryStatusHistoryRepository, times(1)).save(argThat(history ->
				history.getStatus() == DeliveryStatus.SHIPPING && history.getDeliveryId().equals(deliveryId)
			));

			verify(eventPublisher, times(1)).publishEvent(argThat((Object event) ->
				event instanceof DeliveryStatusEvent &&
					((DeliveryStatusEvent) event).status() == DeliveryStatus.SHIPPING
			));
		}

		@Test
		@DisplayName("실패: 존재하지 않는 택배사 코드로 배송 출발 시도 시 예외 발생")
		void fail_shipping_invalid_courier() {
			// given
			Long deliveryId = 500L;
			Delivery delivery = mockDelivery(deliveryId, DeliveryStatus.READY);
			var request = new DeliveryRequest.UpdateStatus(DeliveryStatus.SHIPPING, "WRONG_CODE", "123");

			given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(delivery));

			given(courierRepository.existsByCode("WRONG_CODE")).willReturn(false);

			// when & then
			assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(deliveryId, request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURIER_NOT_FOUND);

			// 이력 저장 안 됐는지 확인
			verify(deliveryStatusHistoryRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패: 존재하지 않는 택배사 코드로 배송 출발 시도 시 예외 발생")
		void fail_shipping_invalid_courier() {
			// given
			Long deliveryId = 500L;
			Delivery delivery = mockDelivery(deliveryId, DeliveryStatus.READY);
			var request = new DeliveryRequest.UpdateStatus(DeliveryStatus.SHIPPING, "WRONG_CODE", "123");

			given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(delivery));

			given(courierRepository.existsByCode("WRONG_CODE")).willReturn(false);

			// when & then
			assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(deliveryId, request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURIER_NOT_FOUND);

			// 이력 저장 안 됐는지 확인
			verify(deliveryStatusHistoryRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패: 배송 출발(SHIPPING)인데 송장 정보가 없으면 예외 발생")
		void fail_shipping_no_tracking_info() {
			// given
			Long deliveryId = 500L;
			Delivery delivery = mockDelivery(deliveryId, DeliveryStatus.READY);

			var request = new DeliveryRequest.UpdateStatus(
				DeliveryStatus.SHIPPING, null, null // 송장 누락
			);

			given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(delivery));

			// when & then
			assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(deliveryId, request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMON_INVALID_ARGUMENT);

			// 이력 저장은 호출되지 않아야 함
			verify(deliveryStatusHistoryRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패: 순서를 위반한 상태 변경은 도메인 로직에 의해 거부된다")
		void fail_invalid_transition() {
			// given
			Long deliveryId = 500L;
			Delivery delivery = mockDelivery(deliveryId, DeliveryStatus.PENDING); // 아직 PENDING

			// PENDING -> SHIPPING (점프) 시도
			var request = new DeliveryRequest.UpdateStatus(
				DeliveryStatus.SHIPPING, "CJ", "12345"
			);

			given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(delivery));

			given(courierRepository.existsByCode("CJ")).willReturn(true);

			// when & then
			assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(deliveryId, request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELIVERY_NOT_IN_READY); // 엔티티 검증 에러
		}
	}
}