package com.kt.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DeliveryAddressRequest {
	public record Create(
		@NotBlank(message = "배송지명은 필수입니다.")
		String addressName,
		@NotBlank(message = "수령인명은 필수입니다.")
		String receiverName,
		@NotBlank(message = "수령인 연락처는 필수입니다.")
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile,
		@NotBlank(message = "우편번호는 필수입니다.")
		String postalCode,
		@NotBlank(message = "도로명주소는 필수입니다.")
		String roadAddress,

		String detailAddress,
		Boolean isDefault
	){

	}

	public record Update(
		String addressName,
		String receiverName,
		String receiverMobile,
		String postalCode,
		String roadAddress,
		String detailAddress
	){

	}
}
