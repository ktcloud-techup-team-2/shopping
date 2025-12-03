package com.kt.service.category;

import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import com.kt.dto.category.CategoryResponse;
import com.kt.repository.category.CategoryRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryQueryService {

	private final CategoryRepository categoryRepository;

	public CategoryResponse.UserLevels getLevels(PetType petType) {
		List<Category> categories = categoryRepository.findAllByStatusAndPetType(CategoryStatus.ACTIVE, petType);

		Map<Integer, List<CategoryResponse.UserLevels.UserCategory>> grouped = new LinkedHashMap<>();
		for (Category category : categories) {
			grouped.computeIfAbsent(category.getDepth(), d -> new ArrayList<>())
				.add(new CategoryResponse.UserLevels.UserCategory(
					category.getId(),
					category.getName(),
					category.getParent() == null ? null : category.getParent().getId(),
					category.getPetType(),
					category.getSortOrder()
				));
		}

		List<CategoryResponse.UserLevels.Level> levels = grouped.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new CategoryResponse.UserLevels.Level(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		return new CategoryResponse.UserLevels(levels);
	}
}