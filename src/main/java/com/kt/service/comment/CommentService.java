package com.kt.service.comment;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.board.Board;
import com.kt.domain.comment.Comment;
import com.kt.domain.user.User;
import com.kt.dto.comment.CommentRequest;
import com.kt.dto.comment.CommentResponse;
import com.kt.repository.board.BoardRepository;
import com.kt.repository.comment.CommentRepository;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final BoardRepository boardRepository;

	public CommentResponse.Simple createComment(Long userId, Long boardId, CommentRequest.Create request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

		Comment comment = Comment.write(board, user, request.content());
		Comment savedComment = commentRepository.save(comment);

		return CommentResponse.Simple.from(savedComment);
	}

	public Page<CommentResponse.Simple> getCommentList(Long boardId, Pageable pageable) {
		if (!boardRepository.existsById(boardId)) {
			throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
		}
		return commentRepository.findAllByBoardIdAndDeletedFalse(boardId, pageable)
			.map(CommentResponse.Simple::from);
	}

	public CommentResponse.Simple updateComment(Long userId, Long commentId, CommentRequest.Update request) {
		Comment comment = findCommentById(commentId);

		validateWriter(comment, userId);

		comment.update(request.content());

		return CommentResponse.Simple.from(comment);
	}

	public void deleteComment(Long userId, Long commentId) {
		Comment comment = findCommentById(commentId);

		validateWriter(comment, userId);

		comment.delete(userId);
	}

	public void deleteCommentByAdmin(Long adminId, Long commentId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		comment.delete(adminId);
	}

	private Comment findCommentById(Long commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
	}

	private void validateWriter(Comment comment, Long userId) {
		Preconditions.validate(
			comment.getUser().getId().equals(userId),
			ErrorCode.COMMENT_NOT_WRITER
		);
	}
}