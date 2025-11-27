package com.kt.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductRequest {
	public record Create(
		@NotBlank(message = "상품명은 필수 값입니다.")
		@Size(max = 200, message = "상품명은 최대 200자까지 입력 가능합니다.")
		String name,

		@Size(max = 2000, message = "상품 설명은 최대 2000자까지 입력 가능합니다.")
		String description,

		@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
		int price,

		@Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
		int stockQuantity
	) {}
}
