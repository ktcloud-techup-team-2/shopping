package com.kt.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductRequest {
	public record Create(
		@NotBlank(message = "상품명은 필수 값입니다.")
		@Size(max = 200, message = "상품명은 최대 200자까지 입력 가능합니다.")
		String name,

		@NotBlank(message = "상품 설명은 필수입니다.")
		String description,

		@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
		int price
	) {}
	public record Update(
		@NotBlank(message = "상품명은 필수입니다.")
		String name,

		@NotBlank(message = "상품 설명은 필수입니다.")
		String description,

		@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
		int price
	) {}
}
