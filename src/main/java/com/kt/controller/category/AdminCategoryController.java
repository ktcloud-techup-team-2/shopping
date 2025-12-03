package com.kt.controller.category;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.pet.PetType;
import com.kt.dto.category.CategoryRequest;
import com.kt.dto.category.CategoryResponse;
import com.kt.service.category.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

	private final AdminCategoryService adminCategoryService;

	@PostMapping
	public ApiResponseEntity<CategoryResponse.Detail> create(@RequestBody @Valid CategoryRequest.Create request) {
		var response = adminCategoryService.create(request);
		return ApiResponseEntity.created(response);
	}

	@PutMapping("/{id}")
	public ApiResponseEntity<CategoryResponse.Detail> update(
		@PathVariable Long id,
		@RequestBody @Valid CategoryRequest.Update request
	) {
		var response = adminCategoryService.update(id, request);
		return ApiResponseEntity.success(response);
	}

	@DeleteMapping("/{id}")
	public ApiResponseEntity<Void> delete(@PathVariable Long id) {
		adminCategoryService.delete(id);
		return ApiResponseEntity.empty();
	}

	@GetMapping("/{id}")
	public ApiResponseEntity<CategoryResponse.Detail> getDetail(@PathVariable Long id) {
		var response = adminCategoryService.getDetail(id);
		return ApiResponseEntity.success(response);
	}

	@GetMapping("/tree")
	public ApiResponseEntity<CategoryResponse.Tree> getTree(@RequestParam(required = false) PetType petType) {
		var response = adminCategoryService.getTree(petType);
		return ApiResponseEntity.success(response);
	}
}