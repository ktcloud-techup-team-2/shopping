package com.kt.service.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.delivery.Courier;
import com.kt.dto.delivery.CourierRequest;
import com.kt.repository.delivery.CourierRepository;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

	@InjectMocks
	private CourierService courierService;

	@Mock
	private CourierRepository courierRepository;

	@Nested
	@DisplayName("택배사 등록 (createCourier)")
	class CreateTest {

		@Test
		@DisplayName("성공: 중복되지 않은 코드로 택배사 등록 시 성공한다")
		void success() {
			// given
			var request = new CourierRequest.Create("CJ", "CJ대한통운");

			// 중복 없음
			given(courierRepository.existsByCode("CJ")).willReturn(false);

			// 저장 Mock (ID가 1로 세팅된 객체 반환)
			given(courierRepository.save(any(Courier.class))).willAnswer(inv -> {
				Courier c = inv.getArgument(0);
				ReflectionTestUtils.setField(c, "id", 1L);
				return c;
			});

			// when
			var response = courierService.createCourier(request);

			// then
			assertThat(response.code()).isEqualTo("CJ");
			assertThat(response.name()).isEqualTo("CJ대한통운");
			assertThat(response.isActive()).isTrue(); // 기본값 활성 확인

			verify(courierRepository).save(any(Courier.class));
		}

		@Test
		@DisplayName("실패: 이미 존재하는 택배사 코드인 경우 예외 발생")
		void fail_duplicate_code() {
			// given
			var request = new CourierRequest.Create("CJ", "CJ대한통운");

			// 이미 존재함(true)
			given(courierRepository.existsByCode("CJ")).willReturn(true);

			// when & then
			assertThatThrownBy(() -> courierService.createCourier(request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURIER_CODE_DUPLICATED);

			// save는 호출되면 안 됨
			verify(courierRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("택배사 수정 (updateCourier)")
	class UpdateTest {

		@Test
		@DisplayName("성공: 택배사 명칭과 사용 여부를 수정한다")
		void success() {
			// given
			Long courierId = 1L;
			Courier courier = Courier.create("CJ", "CJ대한통운");
			ReflectionTestUtils.setField(courier, "id", courierId);

			var request = new CourierRequest.Update("CJ택배", false); // 이름 변경, 비활성화

			given(courierRepository.findById(courierId)).willReturn(Optional.of(courier));

			// when
			var response = courierService.updateCourier(courierId, request);

			// then
			assertThat(response.name()).isEqualTo("CJ택배");
			assertThat(response.isActive()).isFalse();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 택배사 ID 수정 시 예외 발생")
		void fail_not_found() {
			// given
			Long courierId = 999L;
			var request = new CourierRequest.Update("이름", true);

			given(courierRepository.findById(courierId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> courierService.updateCourier(courierId, request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURIER_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("택배사 삭제 (deleteCourier)")
	class DeleteTest {

		@Test
		@DisplayName("성공: 택배사를 삭제한다")
		void success() {
			// given
			Long courierId = 1L;
			Courier courier = Courier.create("CJ", "CJ대한통운");

			given(courierRepository.findById(courierId)).willReturn(Optional.of(courier));

			// when
			courierService.deleteCourier(courierId);

			// then
			verify(courierRepository).delete(courier);
		}
	}

	@Nested
	@DisplayName("택배사 목록 조회 (getCourierList)")
	class ListTest {
		@Test
		@DisplayName("성공: 전체 목록을 조회한다")
		void success() {
			// given
			Courier c1 = Courier.create("CJ", "CJ");
			Courier c2 = Courier.create("POST", "우체국");

			given(courierRepository.findAll()).willReturn(List.of(c1, c2));

			// when
			var list = courierService.getCourierList();

			// then
			assertThat(list).hasSize(2);
			assertThat(list.get(0).code()).isEqualTo("CJ");
			assertThat(list.get(1).code()).isEqualTo("POST");
		}
	}
}