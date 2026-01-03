package com.kt.domain.board;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardCategory {
	FREE("자유"),
	INFO("정보"),
	QNA("질문"),
	REVIEW("후기");

	private final String description;
}
