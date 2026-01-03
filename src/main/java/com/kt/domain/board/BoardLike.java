package com.kt.domain.board;

import com.kt.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
	name = "board_likes",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_board_like_user_board",
			columnNames = {"user_id", "board_id"}
		)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private BoardLike(Board board, User user) {
		this.board = board;
		this.user = user;
	}

	public static BoardLike create(Board board, User user) {
		return new BoardLike(board, user);
	}
}