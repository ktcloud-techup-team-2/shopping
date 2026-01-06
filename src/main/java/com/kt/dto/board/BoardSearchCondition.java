package com.kt.dto.board;

import com.kt.domain.board.BoardCategory;

public record BoardSearchCondition(
	BoardCategory category,
	String keyword
) {}
