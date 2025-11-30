package com.kt.domain.delivery;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourierTest {

	@Test
	@DisplayName("택배사 생성 성공")
	void create_success() {
		// when
		Courier courier = Courier.create("CJ", "CJ대한통운");

		// then
		assertThat(courier.getCode()).isEqualTo("CJ");
		assertThat(courier.getName()).isEqualTo("CJ대한통운");
		assertThat(courier.getIsActive()).isTrue(); // 기본값 true 확인
	}

	@Test
	@DisplayName("택배사 정보 수정 성공")
	void update_success() {
		// given
		Courier courier = Courier.create("CJ", "CJ대한통운");

		// when
		courier.update("CJ GLS", false); // 이름 변경, 비활성화

		// then
		assertThat(courier.getName()).isEqualTo("CJ GLS");
		assertThat(courier.getIsActive()).isFalse();
	}
}