package com.kt.service.category;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.category.Category;
import com.kt.domain.pet.PetType;
import com.kt.dto.category.CategoryRequest;
import com.kt.dto.category.CategoryResponse;
import com.kt.dto.tree.TreeMapper;
import com.kt.repository.category.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCategoryService {

	private final CategoryRepository categoryRepository;
	private final org.springframework.data.domain.AuditorAware<Long> auditorAware;

	public CategoryResponse.Detail create(CategoryRequest.Create request) {
		Category parent = resolveParent(request.parentId());
		Category category = parent == null
			? Category.createRoot(request.name(), request.sortOrder(), request.status(), request.petType())
			: Category.createChild(parent, request.name(), request.sortOrder(), request.status(), request.petType());

		Category saved = categoryRepository.save(category);
		return CategoryResponse.Detail.from(saved);
	}

	public CategoryResponse.Detail update(Long id, CategoryRequest.Update request) {
		Category category = getNonDeletedOrThrow(id);
		Category parent = resolveParent(request.parentId());

		category.rename(request.name());
		category.changeSortOrder(request.sortOrder());
		category.changeStatus(request.status());
		category.changePetType(request.petType());
		if (category.getParent() != parent) {
			category.changeParent(parent);
		}

		return CategoryResponse.Detail.from(category);
	}

	public void delete(Long id) {
		Category category = getNonDeletedOrThrow(id);
		Long deleterId = auditorAware.getCurrentAuditor().orElse(null);
		category.softDelete(deleterId);
	}

	@Transactional(readOnly = true)
	public CategoryResponse.Detail getDetail(Long id) {
		return CategoryResponse.Detail.from(getNonDeletedOrThrow(id));
	}

	@Transactional(readOnly = true)
	public CategoryResponse.Tree getTree(PetType petType) {
		List<Category> categories = categoryRepository.findAllForAdmin(petType);
		return CategoryResponse.Tree.builder()
			.tree(TreeMapper.fromCategories(categories))
			.build();
	}

	private Category resolveParent(Long parentId) {
		if (parentId == null) {
			return null;
		}
		return categoryRepository.findByIdAndDeletedFalse(parentId)
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
	}

	private Category getNonDeletedOrThrow(Long id) {
		return categoryRepository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
	}
}