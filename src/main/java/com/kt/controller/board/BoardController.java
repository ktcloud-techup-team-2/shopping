package com.kt.controller.board;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.board.BoardRequest;
import com.kt.dto.board.BoardResponse;
import com.kt.security.AuthUser;
import com.kt.service.board.BoardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
	private final BoardService boardService;

	@PostMapping
	public ApiResponseEntity<Long> createBoard(
		@AuthenticationPrincipal AuthUser user,
		@RequestBody @Valid BoardRequest.Create request
	) {
		Long boardId = boardService.createBoard(user.id(), request);

		return ApiResponseEntity.created(boardId);
	}

	@GetMapping
	public ApiResponseEntity<List<BoardResponse.Simple>> getBoardList(
		@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponseEntity.pageOf(boardService.getBoardList(pageable));
	}

	@GetMapping("/{boardId}")
	public ApiResponseEntity<BoardResponse.Detail> getBoardDetail(@PathVariable Long boardId) {
		return ApiResponseEntity.success(boardService.getBoardDetail(boardId));
	}

	@PutMapping("/{boardId}")
	public ApiResponseEntity<Void> updateBoard(
		@AuthenticationPrincipal AuthUser user,
		@PathVariable Long boardId,
		@RequestBody @Valid BoardRequest.Update request
	) {
		boardService.updateBoard(user.id(), boardId, request);
		return ApiResponseEntity.success();
	}

	@DeleteMapping("/{boardId}")
	public ApiResponseEntity<Void> deleteBoard(
		@AuthenticationPrincipal AuthUser user,
		@PathVariable Long boardId
	) {
		boardService.deleteBoard(user.id(), boardId);
		return ApiResponseEntity.success();
	}
}
