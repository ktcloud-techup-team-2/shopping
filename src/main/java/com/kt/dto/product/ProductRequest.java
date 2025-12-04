package com.kt.dto.product;

import java.util.List;

import com.kt.domain.pet.PetType;
import com.kt.domain.product.ProductStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequest {
	public record Create(
		@NotBlank(message = "상품명은 필수 값입니다.")
		@Size(max = 200, message = "상품명은 최대 200자까지 입력 가능합니다.")
		String name,

		@NotBlank(message = "상품 설명은 필수입니다.")
		String description,

		@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
		int price,

		@NotNull(message = "반려동물 분류는 필수입니다.")
		PetType petType,

		@NotEmpty(message = "카테고리를 한 개 이상 선택해주세요.")
		List<Long> categoryIds,

		boolean activateImmediately
	) {}
	public record Update(
		@NotBlank(message = "상품명은 필수입니다.")
		String name,

		@NotBlank(message = "상품 설명은 필수입니다.")
		String description,

		@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
		int price,

		@NotNull(message = "반려동물 분류는 필수입니다.")
		PetType petType,

		@NotEmpty(message = "카테고리를 한 개 이상 선택해주세요.")
		List<Long> categoryIds
	) {}
	public record BulkSoldOut(
		@NotEmpty(message = "품절 처리할 상품을 한 개 이상 선택해주세요.")
		List<Long> productIds
	) {}
	public record SearchCond(
		String name,
		PetType petType,
		ProductStatus status
	) {}
}