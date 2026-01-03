package com.kt.repository.board;

import com.kt.domain.board.Board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> , BoardRepositoryCustom {
	Page<Board> findAllByDeletedFalse(Pageable pageable);
}