package com.kt.domain.delivery;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.dto.delivery.DeliveryAddressRequest;

class DeliveryAddressTest {

	@Test
	void 배송지_생성() {
		Long userId = 1L;
		var request = new DeliveryAddressRequest.Create(
			"우리집",
			"홍길동",
			"010-1234-5678",
			"12345",
			"서울 강남구 테헤란로",
			"101호",
			true
		);

		DeliveryAddress address = DeliveryAddress.from(userId, request);

		assertThat(address.getUserId()).isEqualTo(userId);
		assertThat(address.getAddressName()).isEqualTo("우리집");
		assertThat(address.getReceiverName()).isEqualTo("홍길동");
		assertThat(address.getReceiverMobile()).isEqualTo("010-1234-5678");
		assertThat(address.getPostalCode()).isEqualTo("12345");
		assertThat(address.getRoadAddress()).isEqualTo("서울 강남구 테헤란로");
		assertThat(address.getDetailAddress()).isEqualTo("101호");

		assertThat(address.getIsDefault()).isTrue();
		assertThat(address.getIsActive()).isTrue();
	}

	@Test
	void 배송지_수정() {
		DeliveryAddress address = new DeliveryAddress(
			1L, "집", "홍길동", "010-1234-5678",
			"12345", "서울", "상세", false, true
		);

		address.update(
			"회사",
			"김철수",
			"010-9876-5432",
			"54321",
			"부산 해운대구",
			"오피스텔"
		);

		assertThat(address.getAddressName()).isEqualTo("회사");
		assertThat(address.getReceiverName()).isEqualTo("김철수");
		assertThat(address.getReceiverMobile()).isEqualTo("010-9876-5432");
		assertThat(address.getPostalCode()).isEqualTo("54321");
		assertThat(address.getRoadAddress()).isEqualTo("부산 해운대구");
		assertThat(address.getDetailAddress()).isEqualTo("오피스텔");
	}

	@Test
	@DisplayName("기본 배송지 설정/해제 테스트")
	void 기본_배송지_설정() {
		DeliveryAddress address = new DeliveryAddress();

		address.setAsDefault();

		assertThat(address.getIsDefault()).isTrue();

		address.unsetAsDefault();

		assertThat(address.getIsDefault()).isFalse();
	}

	@Test
	void 배송지_삭제() {
		// given
		DeliveryAddress address = new DeliveryAddress();

		// when
		address.deactivate();

		// then
		assertThat(address.getIsActive()).isFalse();
	}

	@Test
	void 배송지_소유쟈_확인() {
		Long myId = 1L;
		Long otherId = 2L;

		DeliveryAddress address = new DeliveryAddress(
			myId, "집", "나", "010", "123", "주소", "상세", false, true
		);

		assertThat(address.isOwnedBy(myId)).isTrue();
		assertThat(address.isOwnedBy(otherId)).isFalse();
	}
}