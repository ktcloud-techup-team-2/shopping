package com.kt.domain.category;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.pet.PetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

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

	private Category(String name, Category parent, int depth, int sortOrder, CategoryStatus status, PetType petType) {
		this.name = validateName(name);
		this.parent = parent;
		this.depth = depth;
		this.sortOrder = sortOrder;
		this.status = status;
		this.petType = petType;
	}

	public static Category createRoot(String name, int sortOrder, CategoryStatus status, PetType petType) {
		Preconditions.validate(petType != null, ErrorCode.CATEGORY_PET_TYPE_REQUIRED);
		Preconditions.validate(status != null, ErrorCode.CATEGORY_STATUS_REQUIRED);
		return new Category(name, null, 1, sortOrder, status, petType);
	}

	public static Category createChild(Category parent, String name, int sortOrder, CategoryStatus status, PetType petType) {
		if (parent == null) {
			throw new IllegalArgumentException("parent cannot be null");
		}
		Preconditions.validate(petType != null, ErrorCode.CATEGORY_PET_TYPE_REQUIRED);
		Preconditions.validate(status != null, ErrorCode.CATEGORY_STATUS_REQUIRED);
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
		this.parent = newParent;
		this.depth = (newParent == null ? 1 : newParent.getDepth() + 1);
	}

	public void changeStatus(CategoryStatus newStatus) {
		Preconditions.validate(newStatus != null, ErrorCode.CATEGORY_STATUS_REQUIRED);
		assertNotDeleted();
		this.status = newStatus;
	}

	public void changePetType(PetType newPetType) {
		Preconditions.validate(newPetType != null, ErrorCode.CATEGORY_PET_TYPE_REQUIRED);
		assertNotDeleted();
		this.petType = newPetType;
	}

	public void softDelete(Long deleterId) {
		assertNotDeleted();
		this.status = CategoryStatus.INACTIVE;
		markDeleted(deleterId);
	}

	private String validateName(String value) {
		Preconditions.validate(Strings.isNotBlank(value), ErrorCode.CATEGORY_NAME_REQUIRED);
		Preconditions.validate(value.length() <= 100, ErrorCode.CATEGORY_NAME_TOO_LONG);
		return value;
	}

	private void assertNotDeleted() {
		if (deleted) {
			throw new CustomException(ErrorCode.CATEGORY_ALREADY_DELETED);
		}
	}
}