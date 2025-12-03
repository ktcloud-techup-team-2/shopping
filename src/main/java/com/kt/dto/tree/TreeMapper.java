package com.kt.dto.tree;

import com.kt.domain.category.Category;
import com.kt.dto.tree.TreeResponse.TreeNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class TreeMapper {

	private TreeMapper() {
	}

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

	public static <T> TreeResponse toTreeResponse(
		List<T> sources,
		Function<T, String> idExtractor,
		Function<T, String> labelExtractor,
		Function<T, String> parentIdExtractor,
		Function<T, Integer> sortOrderExtractor,
		Function<T, String> keywordsExtractor
	) {
		Map<String, TreeNode> nodeMap = new LinkedHashMap<>();
		List<String> rootIds = new ArrayList<>();

		for (T source : sources) {
			String id = idExtractor.apply(source);
			String parentId = parentIdExtractor.apply(source);

			TreeNode node = TreeNode.builder()
				.id(id)
				.label(labelExtractor.apply(source))
				.parentId(parentId)
				.childrenIds(new ArrayList<>())
				.keywords(keywordsExtractor.apply(source))
				.sortOrder(Objects.requireNonNullElse(sortOrderExtractor.apply(source), 0))
				.build();

			nodeMap.put(id, node);
		}

		for (T source : sources) {
			String id = idExtractor.apply(source);
			String parentId = parentIdExtractor.apply(source);

			if (parentId == null) {
				rootIds.add(id);
			} else {
				TreeNode parent = nodeMap.get(parentId);
				if (parent != null) {
					parent.getChildrenIds().add(id);
				}
			}
		}

		sortHierarchy(rootIds, nodeMap);

		return TreeResponse.builder()
			.rootIds(rootIds)
			.nodesById(nodeMap)
			.build();
	}

	private static void sortHierarchy(List<String> rootIds, Map<String, TreeNode> nodeMap) {
		Comparator<String> sortByOrder = Comparator
			.comparing((String id) -> nodeMap.get(id).getSortOrder())
			.thenComparing(Function.identity());

		rootIds.sort(sortByOrder);
		nodeMap.values().forEach(node -> node.getChildrenIds().sort(sortByOrder));
	}
}