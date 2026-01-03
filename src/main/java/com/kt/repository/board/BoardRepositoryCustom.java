package com.kt.repository.board;

import com.kt.domain.board.Board;
import com.kt.dto.board.BoardSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {
	Page<Board> search(BoardSearchCondition condition, Pageable pageable);
}