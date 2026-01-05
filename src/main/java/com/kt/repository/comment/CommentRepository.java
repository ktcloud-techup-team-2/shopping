package com.kt.repository.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.comment.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	Page<Comment> findAllByBoardIdAndDeletedFalse(Long boardId, Pageable pageable);
}
