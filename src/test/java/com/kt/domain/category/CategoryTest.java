package com.kt.domain.category;

import static org.assertj.core.api.Assertions.*;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.PetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryTest {

	@Test
	void 루트_카테고리를_생성하면_깊이_부모_상태_반려동물이_설정된다() {
		// when
		Category root = Category.createRoot("루트", 1, CategoryStatus.ACTIVE, PetType.DOG);

		// then
		assertThat(root.getDepth()).isEqualTo(1);
		assertThat(root.getParent()).isNull();
		assertThat(root.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
		assertThat(root.getPetType()).isEqualTo(PetType.DOG);
	}

	@Test
	void 자식_카테고리를_생성하면_부모와_깊이가_설정된다() {
		// given
		Category parent = Category.createRoot("부모", 1, CategoryStatus.ACTIVE, PetType.CAT);

		// when
		Category child = Category.createChild(parent, "자식", 2, CategoryStatus.INACTIVE, PetType.CAT);

		// then
		assertThat(child.getParent()).isSameAs(parent);
		assertThat(child.getDepth()).isEqualTo(parent.getDepth() + 1);
		assertThat(child.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
	}

	@Test
	void 부모를_변경하면_부모와_깊이가_함께_변경된다() {
		// given
		Category originalParent = Category.createRoot("부모1", 1, CategoryStatus.ACTIVE, PetType.DOG);
		Category newParent = Category.createRoot("부모2", 2, CategoryStatus.ACTIVE, PetType.DOG);
		Category child = Category.createChild(originalParent, "자식", 1, CategoryStatus.ACTIVE, PetType.DOG);

		// when
		child.changeParent(newParent);

		// then
		assertThat(child.getParent()).isSameAs(newParent);
		assertThat(child.getDepth()).isEqualTo(newParent.getDepth() + 1);
	}

	@Test
	void 카테고리를_소프트_삭제하면_비활성화되고_삭제_정보가_기록된다() {
		// given
		Category category = Category.createRoot("삭제 대상", 1, CategoryStatus.ACTIVE, PetType.DOG);

		// when
		category.softDelete(123L);

		// then
		assertThat(category.isDeleted()).isTrue();
		assertThat(category.getDeletedBy()).isEqualTo(123L);
		assertThat(category.getDeletedAt()).isNotNull();
		assertThat(category.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
	}

	@Test
	void 삭제된_카테고리를_수정하면_예외가_발생한다() {
		// given
		Category category = Category.createRoot("삭제 대상", 1, CategoryStatus.ACTIVE, PetType.DOG);
		category.softDelete(null);

		// expected
		assertThatThrownBy(() -> category.rename("변경 불가"))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.CATEGORY_ALREADY_DELETED.getMessage());
	}
}