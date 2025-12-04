package com.kt.controller.category;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import com.kt.dto.category.CategoryResponse;
import com.kt.repository.category.CategoryRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CategoryControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/categories";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void 분류_레벨_리스트_API_성공() throws Exception {
		// given
		Category root = categoryRepository.save(
			Category.createRoot("루트", 1, CategoryStatus.ACTIVE, PetType.DOG)
		);
		categoryRepository.save(
			Category.createChild(root, "자식1", 1, CategoryStatus.ACTIVE, PetType.DOG)
		);
		categoryRepository.save(
			Category.createChild(root, "자식2", 2, CategoryStatus.ACTIVE, PetType.DOG)
		);
		categoryRepository.save(
			Category.createRoot("다른 분류", 3, CategoryStatus.INACTIVE, PetType.DOG)
		);

		var levels = buildLevels();
		var docsResponse = ApiResponse.of(new CategoryResponse.UserLevels(levels));

		// when & then
		mockMvc.perform(
				restDocsFactory.createRequest(
						DEFAULT_URL + "/levels",
						null,
						HttpMethod.GET,
						objectMapper
					).param("petType", PetType.DOG.name())
					.with(jwtUser())
			)
			.andExpect(status().isOk())
			.andDo(
				restDocsFactory.success(
					"categories-levels",
					"카테고리 레벨 조회",
					"사용자 카테고리 레벨별 조회 API",
					"Category",
					null,
					docsResponse
				)
			);
	}

	private List<CategoryResponse.UserLevels.Level> buildLevels() {
		List<Category> categories = categoryRepository.findAllByStatusAndPetType(CategoryStatus.ACTIVE, PetType.DOG);
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

		return grouped.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new CategoryResponse.UserLevels.Level(entry.getKey(), entry.getValue()))
			.toList();
	}
}