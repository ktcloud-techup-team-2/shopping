package com.kt.dto.category;

import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import com.kt.dto.tree.TreeResponse;
import java.util.List;
import lombok.Builder;

public class CategoryResponse {

	public record Detail(
		Long id,
		String name,
		Long parentId,
		int depth,
		int sortOrder,
		CategoryStatus status,
		PetType petType
	) {
		public static Detail from(Category category) {
			return new Detail(
				category.getId(),
				category.getName(),
				category.getParent() == null ? null : category.getParent().getId(),
				category.getDepth(),
				category.getSortOrder(),
				category.getStatus(),
				category.getPetType()
			);
		}
	}

	@Builder
	public record Tree(TreeResponse tree) {
	}

	public record UserLevels(List<Level> levels) {
		public record Level(int depth, List<UserCategory> categories) {
		}

		public record UserCategory(
			Long id,
			String name,
			Long parentId,
			PetType petType,
			int sortOrder
		) {
		}
	}
}