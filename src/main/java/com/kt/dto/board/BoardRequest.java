package com.kt.dto.board;

import com.kt.domain.board.BoardCategory;
import com.kt.domain.pet.PetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BoardRequest {

	public record Create(
		@NotBlank(message = "제목은 필수입니다.")
		String title,

		@NotBlank(message = "내용은 필수입니다.")
		String content,

		@NotNull(message = "카테고리를 선택해주세요.")
		BoardCategory boardCategory,

		@NotNull(message = "반려동물 타입을 선택해주세요.")
		PetType petType,

		List<String> imageUrls
	) {}

	public record Update(
		@NotBlank(message = "변경할 제목은 필수입니다.")
		String title,

		@NotBlank(message = "변경할 내용은 필수입니다.")
		String content,

		@NotNull(message = "카테고리를 선택해주세요.")
		BoardCategory boardCategory,

		@NotNull(message = "반려동물 타입을 선택해주세요.")
		PetType petType,

		List<String> imageUrls
	) {}
}