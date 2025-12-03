package com.kt.domain.category;

import static org.assertj.core.api.Assertions.*;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.PetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryTest {

	@Test
	@DisplayName("루트 카테고리를 생성하면 깊이는 1이고 상태와 반려동물 분류가 설정된다")
	void createRoot_setsDepthAndAttributes() {
		// when
		Category root = Category.createRoot("루트", 1, CategoryStatus.ACTIVE, PetType.DOG);

		// then
		assertThat(root.getDepth()).isEqualTo(1);
		assertThat(root.getParent()).isNull();
		assertThat(root.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
		assertThat(root.getPetType()).isEqualTo(PetType.DOG);
	}

	@Test
	@DisplayName("자식 카테고리를 생성하면 부모와 깊이가 설정된다")
	void createChild_setsParentAndDepth() {
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
	@DisplayName("부모를 변경하면 깊이도 함께 변경된다")
	void changeParent_updatesDepth() {
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
	@DisplayName("소프트 삭제 시 상태는 비활성화되고 삭제 정보가 기록된다")
	void softDelete_marksInactiveAndRecordsDeletion() {
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
	@DisplayName("삭제된 카테고리는 수정할 수 없다")
	void modifyingDeletedCategory_throwsException() {
		// given
		Category category = Category.createRoot("삭제 대상", 1, CategoryStatus.ACTIVE, PetType.DOG);
		category.softDelete(null);

		// expected
		assertThatThrownBy(() -> category.rename("변경 불가"))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.CATEGORY_ALREADY_DELETED.getMessage());
	}
}