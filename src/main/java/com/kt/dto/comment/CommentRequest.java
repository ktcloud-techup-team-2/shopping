package com.kt.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
	public record Create(
		@NotBlank(message = "댓글 내용은 필수입니다.")
		@Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
		String content
	) {}

	public record Update(
		@NotBlank(message = "댓글 내용은 필수입니다.")
		@Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
		String content
	) {}
}