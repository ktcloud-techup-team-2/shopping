package com.kt.domain.comment;

import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.board.Board;
import com.kt.domain.user.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseSoftDeleteEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 1000)
	private String content;

	private Comment(Board board, User user, String content) {
		this.board = board;
		this.user = user;
		this.content = content;
	}

	public static Comment write(Board board, User user, String content) {
		return new Comment(board, user, content);
	}

	public void update(String content) {
		this.content = content;
	}

	public void delete(Long deleterId) {
		this.markDeleted(deleterId);
	}
}