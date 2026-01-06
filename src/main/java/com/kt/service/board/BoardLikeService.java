package com.kt.service.board;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.board.Board;
import com.kt.domain.board.BoardLike;
import com.kt.domain.user.User;
import com.kt.repository.board.BoardLikeRepository;
import com.kt.repository.board.BoardRepository;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardLikeService {

	private final BoardLikeRepository boardLikeRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;

	public void toggleLike(Long boardId, Long userId) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		boardLikeRepository.findByBoardIdAndUserId(boardId, userId)
			.ifPresentOrElse(
				boardLikeRepository::delete,
				() -> boardLikeRepository.save(BoardLike.create(board, user))
			);
	}
}