package com.kt.controller.board;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.board.BoardRequest;
import com.kt.dto.board.BoardResponse;
import com.kt.dto.board.BoardSearchCondition;
import com.kt.security.AuthUser;
import com.kt.service.board.BoardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
	private final BoardService boardService;

	@PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponseEntity<Long> createBoard(
		@AuthenticationPrincipal AuthUser user,
		@RequestPart("request") @Valid BoardRequest.Create request,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		Long boardId = boardService.createBoard(user.id(), request, files);

		return ApiResponseEntity.created(boardId);
	}

	@GetMapping
	public ApiResponseEntity<Page<BoardResponse.Simple>> getBoardList(
		@ModelAttribute BoardSearchCondition condition,
		@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<BoardResponse.Simple> result = boardService.getBoardList(condition, pageable);
		return ApiResponseEntity.success(result);
	}

	@GetMapping("/{boardId}")
	public ApiResponseEntity<BoardResponse.Detail> getBoardDetail(@PathVariable Long boardId) {
		return ApiResponseEntity.success(boardService.getBoardDetail(boardId));
	}

	@PutMapping(value = "/{boardId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponseEntity<Void> updateBoard(
		@AuthenticationPrincipal AuthUser user,
		@PathVariable Long boardId,
		@RequestPart("request") @Valid BoardRequest.Update request,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		boardService.updateBoard(user.id(), boardId, request, files);
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
