package com.kt.controller.category;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.pet.PetType;
import com.kt.dto.category.CategoryResponse;
import com.kt.service.category.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryQueryService categoryQueryService;

	@GetMapping("/levels")
	public ApiResponseEntity<CategoryResponse.UserLevels> getLevels(@RequestParam PetType petType) {
		var response = categoryQueryService.getLevels(petType);
		return ApiResponseEntity.success(response);
	}

	@GetMapping("/tree")
	public ApiResponseEntity<CategoryResponse.Tree> getTree(@RequestParam PetType petType) {
		var response = categoryQueryService.getUserTree(petType);
		return ApiResponseEntity.success(response);
	}
}