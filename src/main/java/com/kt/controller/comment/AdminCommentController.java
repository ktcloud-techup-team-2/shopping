package com.kt.controller.comment;

import com.kt.common.api.ApiResponseEntity;
import com.kt.security.AuthUser;
import com.kt.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

	private final CommentService commentService;

	@DeleteMapping("/{commentId}")
	public ApiResponseEntity<Void> deleteCommentByAdmin(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long commentId
	) {
		commentService.deleteCommentByAdmin(authUser.id(), commentId);
		return ApiResponseEntity.success();
	}
}