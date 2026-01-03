package com.kt.dto.comment;

import java.time.LocalDateTime;
import com.kt.domain.comment.Comment;

public class CommentResponse {

	public record Simple(
		Long commentId,
		String content,
		String writerName,
		Long writerId,
		LocalDateTime createdAt
	) {
		public static Simple from(Comment comment) {
			return new Simple(
				comment.getId(),
				comment.getContent(),
				comment.getUser().getName(),
				comment.getUser().getId(),
				comment.getCreatedAt()
			);
		}
	}
}