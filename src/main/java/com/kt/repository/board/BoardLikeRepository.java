package com.kt.repository.board;

import com.kt.domain.board.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
	Optional<BoardLike> findByBoardIdAndUserId(Long boardId, Long userId);
}