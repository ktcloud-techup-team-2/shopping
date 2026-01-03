package com.kt.controller.board;

import com.kt.common.api.ApiResponseEntity;
import com.kt.security.AuthUser;
import com.kt.service.board.BoardLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoardLikeController {

	private final BoardLikeService boardLikeService;

	@PostMapping("/boards/{boardId}/likes")
	public ApiResponseEntity<Void> toggleLike(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long boardId
	) {
		boardLikeService.toggleLike(boardId, authUser.id());
		return ApiResponseEntity.success();
	}
}