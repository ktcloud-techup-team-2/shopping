package com.kt.domain.delivery;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;

class DeliveryTest {

	@Test
	@DisplayName("배송 생성 시 초기 상태는 PENDING(주문접수)이다")
	void create_default_status() {
		// when
		Delivery delivery = Delivery.create(1L, 100L, 3000);

		// then
		assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
		assertThat(delivery.getDeliveryFee()).isEqualTo(3000);
	}

	@Test
	@DisplayName("정상 흐름: PENDING -> PREPARING -> READY -> SHIPPING -> DELIVERED")
	void status_transition_success() {
		// given
		Delivery delivery = Delivery.create(1L, 100L, 3000);

		// 1. 준비 중
		delivery.startPreparing();
		assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PREPARING);

		// 2. 출고 준비 완료
		delivery.readyForShipment();
		assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.READY);

		// 3. 배송 시작 (송장 입력 가정)
		delivery.updateTrackingInfo("CJ", "12345");
		delivery.ship();
		assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.SHIPPING);
		assertThat(delivery.getShippedAt()).isNotNull();

		// 4. 배송 완료
		delivery.complete();
		assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
		assertThat(delivery.getDeliveredAt()).isNotNull();
	}

	@Test
	@DisplayName("상태 변경 실패: PENDING에서 바로 SHIPPING으로 건너뛸 수 없다")
	void ship_fail_invalid_state() {
		// given
		Delivery delivery = Delivery.create(1L, 100L, 3000); // PENDING

		// when & then
		assertThatThrownBy(delivery::ship)
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELIVERY_NOT_IN_READY);
	}

	@Test
	@DisplayName("주문 취소 성공: 배송 시작 전에는 취소 가능하다")
	void cancel_success() {
		// given
		Delivery delivery = Delivery.create(1L, 100L, 3000); // PENDING

		// when
		delivery.cancel();

		// then
		assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
	}

	@Test
	@DisplayName("주문 취소 실패: 이미 배송 중(SHIPPING)이면 취소할 수 없다")
	void cancel_fail_already_shipping() {
		// given (SHIPPING 상태 만들기)
		Delivery delivery = Delivery.create(1L, 100L, 3000);
		delivery.startPreparing();
		delivery.readyForShipment();
		delivery.ship();

		// when & then
		assertThatThrownBy(delivery::cancel)
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELIVERY_CANCEL_NOT_ALLOWED);
	}
}