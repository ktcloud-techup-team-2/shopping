package com.kt.dto.board;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.domain.board.Board;

public interface BoardResponse {

	record Simple(
		Long boardId,
		String title,
		String category,
		String petType,
		Long userId,
		String thumbnailUrl,
		int viewCount,
		int likeCount,
		LocalDateTime createdAt
	) {
		public static Simple from(Board board) {
			String thumbnail = (board.getImageUrls() != null && !board.getImageUrls().isEmpty())
				? board.getImageUrls().get(0)
				: null;

			return new Simple(
				board.getId(),
				board.getTitle(),
				board.getBoardCategory().name(),
				board.getPetType().name(),
				board.getUser().getId(),
				thumbnail,
				board.getViewCount(),
				board.getLikeCount(),
				board.getCreatedAt()
			);
		}
	}

	record Detail(
		Long boardId,
		String title,
		String content,
		String category,
		String petType,
		Long writerId,
		List<String> imageUrls,
		int viewCount,
		int likeCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		public static Detail from(Board board) {
			return new Detail(
				board.getId(),
				board.getTitle(),
				board.getContent(),
				board.getBoardCategory().name(),
				board.getPetType().name(),
				board.getUser().getId(),
				board.getImageUrls(),
				board.getViewCount(),
				board.getLikeCount(),
				board.getCreatedAt(),
				board.getUpdatedAt()
			);
		}
	}
}