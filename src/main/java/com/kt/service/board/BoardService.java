package com.kt.service.board;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.board.Board;
import com.kt.dto.board.BoardRequest;
import com.kt.dto.board.BoardResponse;
import com.kt.dto.board.BoardSearchCondition;
import com.kt.repository.board.BoardRepository;
import com.kt.domain.user.User;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

	private final BoardRepository boardRepository;
	private final UserRepository userRepository;

	public Long createBoard(Long userId, BoardRequest.Create request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Board board = Board.write(
			user,
			request.title(),
			request.content(),
			request.boardCategory(),
			request.petType(),
			request.imageUrls()
		);

		return boardRepository.save(board).getId();
	}

	public Page<BoardResponse.Simple> getBoardList(BoardSearchCondition condition, Pageable pageable) {
		return boardRepository.search(condition, pageable)
			.map(BoardResponse.Simple::from);
	}

	public BoardResponse.Detail getBoardDetail(Long boardId) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

		board.increaseViewCount();

		return BoardResponse.Detail.from(board);
	}

	public void updateBoard(Long userId, Long boardId, BoardRequest.Update request) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

		Preconditions.validate(board.getUser().getId().equals(userId), ErrorCode.BOARD_NOT_WRITER);

		board.update(
			request.title(),
			request.content(),
			request.boardCategory(),
			request.petType(),
			request.imageUrls()
		);
	}

	@Transactional
	public void deleteBoard(Long userId, Long boardId) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

		Preconditions.validate(board.getUser().getId().equals(userId), ErrorCode.BOARD_NOT_WRITER);

		board.delete(userId);
	}
}