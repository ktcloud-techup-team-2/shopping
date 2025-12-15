package com.kt.service.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.repository.delivery.DeliveryAddressRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceTest {

	@InjectMocks
	private DeliveryAddressService deliveryAddressService;

	@Mock
	private DeliveryAddressRepository deliveryAddressRepository;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(deliveryAddressService, "maxAddressCount", 10);
	}

	@Test
	void 배송지_생성_성공() {
		// given
		Long userId = 1L;
		var request = new DeliveryAddressRequest.Create(
			"집", "홍길동", "010-1234-5678", "12345", "서울", "상세", true // isDefault=true
		);

		// 1. 개수 검사 통과 (현재 5개)
		given(deliveryAddressRepository.countByUserIdAndIsActiveTrue(userId)).willReturn(5);

		// 2. 기존 기본 배송지 조회 및 해제 로직 (Mocking)
		// 기존에 기본 배송지가 있다고 가정 -> unsetAsDefault 호출 여부 확인용
		DeliveryAddress oldDefault = mock(DeliveryAddress.class);
		given(deliveryAddressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId))
			.willReturn(Optional.of(oldDefault));

		// 3. 저장 (입력받은 객체를 그대로 리턴)
		given(deliveryAddressRepository.save(any(DeliveryAddress.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		var response = deliveryAddressService.createAddress(userId, request);

		// then
		assertThat(response.addressName()).isEqualTo("집");
		assertThat(response.isDefault()).isTrue();

		// 검증: 기존 배송지의 기본 설정 해제 메서드가 호출되었는지?
		verify(oldDefault, times(1)).unsetAsDefault();
		// 검증: 저장이 호출되었는지?
		verify(deliveryAddressRepository, times(1)).save(any(DeliveryAddress.class));
	}

	@Test
	void 배송지_생성_실패_최대_개수_초과() {
		// given
		Long userId = 1L;
		var request = new DeliveryAddressRequest.Create("집", "홍", "010", "123", "주소", "상세", false);

		// 현재 이미 10개 (제한 도달)
		given(deliveryAddressRepository.countByUserIdAndIsActiveTrue(userId)).willReturn(10);

		// when & then
		assertThatThrownBy(() -> deliveryAddressService.createAddress(userId, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELIVERY_ADDRESS_MAX_COUNT_EXCEEDED);

		verify(deliveryAddressRepository, never()).save(any());
	}

	@Test
	void 배송지_수정_성공() {
		// given
		Long userId = 1L;
		Long addressId = 100L;
		var request = new DeliveryAddressRequest.Update(
			"회사", "김대리", "010-9999-8888", "54321", "판교", "타워"
		);

		// 기존 배송지 (활성 상태)
		DeliveryAddress mockAddress = new DeliveryAddress(
			userId, "집", "홍", "010", "123", "주소", "상세", false, true
		);

		given(deliveryAddressRepository.findByIdAndUserId(addressId, userId))
			.willReturn(Optional.of(mockAddress));

		// when
		var response = deliveryAddressService.updateAddress(userId, addressId, request);

		// then
		assertThat(response.addressName()).isEqualTo("회사"); // 변경된 값 확인
		assertThat(response.receiverName()).isEqualTo("김대리");
	}

	@Test
	void 배송지_삭제_성공() {
		// given
		Long userId = 1L;
		Long addressId = 100L;

		// 이미 삭제된(isActive=false) 배송지
		DeliveryAddress deletedAddress = new DeliveryAddress(
			userId, "집", "홍", "010", "123", "주소", "상세", false, false // false 주목
		);

		given(deliveryAddressRepository.findByIdAndUserId(addressId, userId))
			.willReturn(Optional.of(deletedAddress));

		// when & then
		assertThatThrownBy(() -> deliveryAddressService.deleteAddress(userId, addressId))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELIVERY_ADDRESS_ALREADY_DELETED);
	}

	@Test
	void 기본_배송지_조회_실패_기본_배송지_없음() {
		// given
		Long userId = 1L;
		given(deliveryAddressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> deliveryAddressService.getDefaultAddress(userId))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEFAULT_DELIVERY_ADDRESS_NOT_SET);
	}
}