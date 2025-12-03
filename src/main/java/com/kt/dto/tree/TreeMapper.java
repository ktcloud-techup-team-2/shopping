package com.kt.dto.tree;

import com.kt.domain.category.Category;
import com.kt.dto.tree.TreeResponse.NodeState;
import com.kt.dto.tree.TreeResponse.TreeNode;

import java.util.*;
import java.util.function.Function;

/**
 * 계층 구조 데이터를 TreeResponse 형태로 변환하는 유틸리티 클래스
 */
public final class TreeMapper {

	private TreeMapper() { }

	/**
	 * Category 리스트를 트리 응답 구조로 변환
	 */
	public static TreeResponse fromCategories(List<Category> categories) {
		return toTreeResponse(
			categories,
			category -> String.valueOf(category.getId()),
			Category::getName,
			category -> category.getParent() == null ? null : String.valueOf(category.getParent().getId()),
			Category::getSortOrder,
			Category::getName
		);
	}

	/**
	 * 제네릭 소스 리스트를 TreeResponse로 변환하는 공통 메서드
	 */
	public static <T> TreeResponse toTreeResponse(
		List<T> sources,
		Function<T, String> idExtractor,
		Function<T, String> labelExtractor,
		Function<T, String> parentIdExtractor,
		Function<T, Integer> sortOrderExtractor,
		Function<T, String> keywordsExtractor
	) {

		// 0) NodeData 버퍼
		Map<String, NodeData> nodeDataMap = new LinkedHashMap<>();
		List<String> rootIds = new ArrayList<>();

		// 1) 소스 → NodeData 변환 (id, parentId, label 등 기본 정보만 수집)
		for (T source : sources) {
			String id = idExtractor.apply(source);
			String parentId = parentIdExtractor.apply(source);

			NodeData data = new NodeData(
				id,
				labelExtractor.apply(source),
				parentId,
				new ArrayList<>(),
				keywordsExtractor.apply(source),
				Objects.requireNonNullElse(sortOrderExtractor.apply(source), 0)
			);

			if (nodeDataMap.put(id, data) != null) {
				throw new IllegalStateException("트리 생성 중 중복된 ID가 발견되었습니다. ID = " + id);
			}
		}

		// 2) 부모-자식 연결 + 루트 수집
		for (NodeData node : nodeDataMap.values()) {
			if (node.parentId() == null) {
				rootIds.add(node.id());
				continue;
			}

			NodeData parent = nodeDataMap.get(node.parentId());
			if (parent != null) {
				parent.childrenIds().add(node.id());
			}
		}

		// 3) 정렬
		sortHierarchy(rootIds, nodeDataMap);

		// 4) NodeData → TreeNode 변환 (여기서 state 세팅)
		Map<String, TreeNode> nodeMap = new LinkedHashMap<>();

		for (NodeData node : nodeDataMap.values()) {
			boolean hasChildren = !node.childrenIds().isEmpty();

			TreeNode treeNode = TreeNode.builder()
				.id(node.id())
				.label(node.label())
				.parentId(node.parentId())
				.childrenIds(node.childrenIds())
				.state(NodeState.builder()
					.opened(hasChildren)
					.checked(false)
					.build()
				)
				.keywords(node.keywords())
				.sortOrder(node.sortOrder())
				.build();

			nodeMap.put(treeNode.id(), treeNode);
		}

		return TreeResponse.builder()
			.rootIds(rootIds)
			.nodesById(nodeMap)
			.build();
	}

	/**
	 * 루트/자식 노드들을 sortOrder 기준으로 정렬
	 */
	private static void sortHierarchy(List<String> rootIds, Map<String, NodeData> nodeDataMap) {
		Comparator<String> sortByOrder = Comparator
			.comparing((String id) -> nodeDataMap.get(id).sortOrder())
			.thenComparing(Function.identity());

		rootIds.sort(sortByOrder);
		nodeDataMap.values().forEach(node -> node.childrenIds().sort(sortByOrder));
	}

	/**
	 * TreeMapper 내부에서만 사용하는 임시 노드 record
	 */
	private record NodeData(
		String id,
		String label,
		String parentId,
		List<String> childrenIds,
		String keywords,
		int sortOrder
	) { }
}
