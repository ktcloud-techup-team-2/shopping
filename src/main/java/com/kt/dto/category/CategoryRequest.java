package com.kt.dto.category;

import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

	public record Create(
		@NotBlank(message = "카테고리 명은 필수입니다.")
		@Size(max = 100, message = "카테고리 명은 100자 이하여야 합니다.")
		String name,

		Long parentId,

		@Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
		int sortOrder,

		@NotNull(message = "상태는 필수입니다.")
		CategoryStatus status,

		@NotNull(message = "반려동물 분류는 필수입니다.")
		PetType petType
	) {}

	public record Update(
		@NotBlank(message = "카테고리 명은 필수입니다.")
		@Size(max = 100, message = "카테고리 명은 100자 이하여야 합니다.")
		String name,

		Long parentId,

		@Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
		int sortOrder,

		@NotNull(message = "상태는 필수입니다.")
		CategoryStatus status,

		@NotNull(message = "반려동물 분류는 필수입니다.")
		PetType petType
	) {}
}