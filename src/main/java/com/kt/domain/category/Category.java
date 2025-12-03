package com.kt.domain.category;

import static com.kt.common.Preconditions.*;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.pet.PetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseSoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Category parent;

	@Column(nullable = false)
	private int depth;

	@Column(nullable = false)
	private int sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoryStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PetType petType;

	private Category(
		String name,
		Category parent,
		int depth,
		int sortOrder,
		CategoryStatus status,
		PetType petType
	) {
		this.name = validateName(name);
		this.parent = parent;
		this.depth = depth;
		this.sortOrder = sortOrder;
		this.status = status;
		this.petType = petType;
	}

	public static Category createRoot(String name, int sortOrder, CategoryStatus status, PetType petType) {
		validateRequiredAttributes(status, petType);
		return new Category(name, null, 1, sortOrder, status, petType);
	}

	public static Category createChild(
		Category parent,
		String name,
		int sortOrder,
		CategoryStatus status,
		PetType petType
	) {
		nullValidate(parent, ErrorCode.COMMON_INVALID_ARGUMENT);
		validateRequiredAttributes(status, petType);

		// 부모가 삭제 상태면 자식 생성 불가
		if (parent.deleted) {
			throw new CustomException(ErrorCode.CATEGORY_ALREADY_DELETED);
		}

		// 부모와 다른 PetType으로 자식 생성 방지
		if (!Objects.equals(parent.petType, petType)) {
			throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}

		return new Category(name, parent, parent.getDepth() + 1, sortOrder, status, petType);
	}

	public void rename(String newName) {
		assertNotDeleted();
		this.name = validateName(newName);
	}

	public void changeSortOrder(int sortOrder) {
		assertNotDeleted();
		this.sortOrder = sortOrder;
	}

	public void changeParent(Category newParent) {
		assertNotDeleted();

		// 자기 자신을 부모로 설정 방지
		if (this == newParent) {
			throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}

		// 부모가 삭제 상태면 변경 불가
		if (newParent != null && newParent.deleted) {
			throw new CustomException(ErrorCode.CATEGORY_ALREADY_DELETED);
		}

		// 부모/자식 간 PetType 일관성 유지
		if (newParent != null && !Objects.equals(newParent.petType, this.petType)) {
			throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}

		// 부모가 동일하면 변경 없음
		if (Objects.equals(this.parent, newParent)) {
			return;
		}

		this.parent = newParent;
		this.depth = (newParent == null ? 1 : newParent.getDepth() + 1);
	}

	public void changeStatus(CategoryStatus newStatus) {
		nullValidate(newStatus, ErrorCode.CATEGORY_STATUS_REQUIRED);
		assertNotDeleted();
		this.status = newStatus;
	}

	public void changePetType(PetType newPetType) {
		nullValidate(newPetType, ErrorCode.CATEGORY_PET_TYPE_REQUIRED);
		assertNotDeleted();
		this.petType = newPetType;
	}

	public void softDelete(Long deleterId) {
		assertNotDeleted();
		this.status = CategoryStatus.INACTIVE;
		markDeleted(deleterId);
	}

	private static void validateRequiredAttributes(CategoryStatus status, PetType petType) {
		nullValidate(status, ErrorCode.CATEGORY_STATUS_REQUIRED);
		nullValidate(petType, ErrorCode.CATEGORY_PET_TYPE_REQUIRED);
	}

	private String validateName(String value) {
		validate(Strings.isNotBlank(value), ErrorCode.CATEGORY_NAME_REQUIRED);
		validate(value.length() <= 100, ErrorCode.CATEGORY_NAME_TOO_LONG);
		return value;
	}

	private void assertNotDeleted() {
		if (deleted) {
			throw new CustomException(ErrorCode.CATEGORY_ALREADY_DELETED);
		}
	}
}