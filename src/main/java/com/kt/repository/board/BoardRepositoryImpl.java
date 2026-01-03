package com.kt.repository.board;

import com.kt.domain.board.Board;
import com.kt.domain.board.BoardCategory;
import com.kt.dto.board.BoardSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.kt.domain.board.QBoard.board;

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Board> search(BoardSearchCondition condition, Pageable pageable) {
		List<Board> content = queryFactory
			.selectFrom(board)
			.where(
				eqCategory(condition.category()),
				containsKeyword(condition.keyword()),
				board.deleted.isFalse()
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(board.createdAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(board.count())
			.from(board)
			.where(
				eqCategory(condition.category()),
				containsKeyword(condition.keyword()),
				board.deleted.isFalse()
			);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression eqCategory(BoardCategory category) {
		if (category == null) {
			return null;
		}
		return board.boardCategory.eq(category);
	}

	private BooleanExpression containsKeyword(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}
		return board.title.contains(keyword).or(board.content.contains(keyword));
	}
}