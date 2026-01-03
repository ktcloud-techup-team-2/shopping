package com.kt.controller.comment;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.comment.CommentRequest;
import com.kt.dto.comment.CommentResponse;
import com.kt.security.AuthUser;
import com.kt.service.comment.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/boards/{boardId}/comments")
	public ApiResponseEntity<CommentResponse.Simple> createComment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long boardId,
		@RequestBody @Valid CommentRequest.Create request
	) {
		CommentResponse.Simple response = commentService.createComment(authUser.id(), boardId, request);
		return ApiResponseEntity.success(response);
	}

	@GetMapping("/boards/{boardId}/comments")
	public ApiResponseEntity<List<CommentResponse.Simple>> getCommentList(
		@PathVariable Long boardId,
		@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponseEntity.pageOf(commentService.getCommentList(boardId, pageable));
	}

	@PutMapping("/comments/{commentId}")
	public ApiResponseEntity<CommentResponse.Simple> updateComment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long commentId,
		@RequestBody @Valid CommentRequest.Update request
	) {
		CommentResponse.Simple response = commentService.updateComment(authUser.id(), commentId, request);
		return ApiResponseEntity.success(response);
	}

	@DeleteMapping("/comments/{commentId}")
	public ApiResponseEntity<Void> deleteComment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long commentId
	) {
		commentService.deleteComment(authUser.id(), commentId);
		return ApiResponseEntity.success();
	}
}