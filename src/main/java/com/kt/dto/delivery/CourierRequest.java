package com.kt.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CourierRequest {
	public record Create(
		@NotBlank(message = "택배사 코드는 필수입니다.")
		String code,

		@NotBlank(message = "택배사 명칭은 필수입니다.")
		String name
	) {}

	public record Update(
		@NotBlank(message = "택배사 명칭은 필수입니다.")
		String name,

		@NotNull(message = "사용 여부는 필수입니다.")
		Boolean isActive
	) {}
}
