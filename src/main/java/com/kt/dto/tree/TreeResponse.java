package com.kt.dto.tree;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeResponse {

	@Getter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class NodeState {
		private final boolean opened;
		private final boolean checked;

		@Builder
		public NodeState(boolean opened, boolean checked) {
			this.opened = opened;
			this.checked = checked;
		}
	}

	@Getter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class TreeNode {
		private final String id;
		private final String label;
		private final String parentId;
		private final List<String> childrenIds;
		private final NodeState state;
		private final String keywords;
		private final int sortOrder;

		@Builder
		public TreeNode(
			String id,
			String label,
			String parentId,
			List<String> childrenIds,
			NodeState state,
			String keywords,
			int sortOrder
		) {
			this.id = id;
			this.label = label;
			this.parentId = parentId;
			this.childrenIds = childrenIds != null ? childrenIds : new ArrayList<>();
			this.state = state;
			this.keywords = keywords;
			this.sortOrder = sortOrder;
		}
	}

	private final List<String> rootIds;
	private final Map<String, TreeNode> nodesById;

	@Builder
	public TreeResponse(List<String> rootIds, Map<String, TreeNode> nodesById) {
		this.rootIds = rootIds != null ? rootIds : new ArrayList<>();
		this.nodesById = nodesById != null ? nodesById : new HashMap<>();
	}
}