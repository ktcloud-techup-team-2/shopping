package com.kt.dto.tree;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 프론트 트리 컴포넌트에 맞춘 공통 트리 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TreeResponse(
	List<String> rootIds,
	Map<String, TreeNode> nodesById
) {

	@Builder
	public TreeResponse {
		rootIds = rootIds != null ? rootIds : new ArrayList<>();
		nodesById = nodesById != null ? nodesById : new HashMap<>();
	}

	/**
	 * 트리 노드의 UI 상태(펼침/선택)를 표현하는 DTO
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record NodeState(boolean opened, boolean checked) {

		@Builder
		public NodeState { }
	}

	/**
	 * 트리의 개별 노드를 표현하는 DTO
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record TreeNode(
		String id,
		String label,
		String parentId,
		List<String> childrenIds,
		NodeState state,
		String keywords,
		int sortOrder
	) {

		@Builder
		public TreeNode {
			childrenIds = childrenIds != null ? childrenIds : new ArrayList<>();}
	}
}
