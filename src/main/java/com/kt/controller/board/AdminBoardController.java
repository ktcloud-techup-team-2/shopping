package com.kt.controller.board;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.board.BoardResponse;
import com.kt.dto.board.BoardSearchCondition;
import com.kt.security.AuthUser;
import com.kt.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/boards")
public class AdminBoardController {

	private final BoardService boardService;

	@GetMapping
	public ApiResponseEntity<Page<BoardResponse.Simple>> getAdminBoardList(
		@ModelAttribute BoardSearchCondition condition,
		@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<BoardResponse.Simple> result = boardService.getBoardList(condition, pageable);
		return ApiResponseEntity.success(result);
	}

	@GetMapping("/{boardId}")
	public ApiResponseEntity<BoardResponse.Detail> getAdminBoardDetail(@PathVariable Long boardId) {
		BoardResponse.Detail result = boardService.getBoardDetail(boardId);
		return ApiResponseEntity.success(result);
	}

	@DeleteMapping("/{boardId}")
	public ApiResponseEntity<Void> deleteBoardByAdmin(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long boardId
	) {
		boardService.deleteBoardByAdmin(authUser.id(), boardId);
		return ApiResponseEntity.success();
	}
}