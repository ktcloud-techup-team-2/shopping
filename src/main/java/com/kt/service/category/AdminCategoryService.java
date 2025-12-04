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
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCategoryService {

	private final CategoryRepository categoryRepository;
	private final AuditorAware<Long> auditorAware;

	public CategoryResponse.Detail create(CategoryRequest.Create request) {

		Optional<Category> parentOpt = findParent(request.parentId());

		Category category = parentOpt
			.map(parent -> Category.createChild(
				parent,
				request.name(),
				request.sortOrder(),
				request.status(),
				request.petType()
			))
			.orElseGet(() -> Category.createRoot(
				request.name(),
				request.sortOrder(),
				request.status(),
				request.petType()
			));

		Category saved = categoryRepository.save(category);
		return CategoryResponse.Detail.from(saved);
	}

	public CategoryResponse.Detail update(Long id, CategoryRequest.Update request) {

		Category category = getExistingCategory(id);
		Optional<Category> newParentOpt = findParent(request.parentId());

		// 공통 속성 변경
		category.rename(request.name());
		category.changeSortOrder(request.sortOrder());
		category.changeStatus(request.status());
		category.changePetType(request.petType());

		// 부모 변경 필요 여부 체크 (Optional 기반 비교)
		Optional<Category> currentParentOpt = Optional.ofNullable(category.getParent());
		if (!currentParentOpt.equals(newParentOpt)) {
			category.changeParent(newParentOpt.orElse(null));
		}

		return CategoryResponse.Detail.from(category);
	}

	public void delete(Long id) {
		Category category = getExistingCategory(id);
		// TODO :: auditorAware 파라미터로 변경 예정
		Long deleterId = auditorAware.getCurrentAuditor().orElse(null);
		category.softDelete(deleterId);
	}

	@Transactional(readOnly = true)
	public CategoryResponse.Detail getDetail(Long id) {
		return CategoryResponse.Detail.from(getExistingCategory(id));
	}

	@Transactional(readOnly = true)
	public CategoryResponse.Tree getTree(PetType petType) {
		List<Category> categories = categoryRepository.findAllForAdmin(petType);
		return CategoryResponse.Tree.builder()
			.tree(TreeMapper.fromCategories(categories))
			.build();
	}

	/**
	 * parentId가 null이면 Optional.empty() → 루트로 간주
	 * parentId가 있으면 해당 부모 카테고리를 찾고, 없으면 예외 발생
	 */
	private Optional<Category> findParent(Long parentId) {
		if (parentId == null) {
			return Optional.empty();
		}
		return categoryRepository.findByIdAndDeletedFalse(parentId)
			.or(() -> {
				throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
			});
	}

	/**
	 * 삭제되지 않은 카테고리를 ID로 조회 (없으면 예외)
	 */
	private Category getExistingCategory(Long id) {
		return categoryRepository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
	}
}