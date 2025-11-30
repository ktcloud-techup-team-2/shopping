package com.kt.domain.product;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseSoftDeleteEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.kt.common.Preconditions.validate;

import org.apache.logging.log4j.util.Strings;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseSoftDeleteEntity {

	@Column(nullable = false, length = 200)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private int price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status;

	private Product(
		String name,
		String description,
		int price
	) {
		this.name = validateName(name);
		this.description = description;
		this.price = validatePrice(price);
		// 최초 등록시 항상 DRAFT(임시 저장)
		this.status = ProductStatus.DRAFT;
	}

	public static Product create(
		String name,
		String description,
		int price
	) {
		return new Product(name, description, price);
	}

	public void update(
		String name,
		String description,
		int price
	) {
		assertNotDeleted();
		this.name = validateName(name);
		this.description = description;
		this.price = validatePrice(price);
	}

	public void activate() {
		changeStatus(ProductStatus.ACTIVE);
	}

	public void inactivate() {
		changeStatus(ProductStatus.INACTIVE);
	}

	public void markSoldOut() {
		changeStatus(ProductStatus.SOLD_OUT);
	}

	public void toggleSoldOut() {
		if (status == ProductStatus.SOLD_OUT) {
			changeStatus(ProductStatus.INACTIVE);
			return;
		}

		changeStatus(ProductStatus.SOLD_OUT);
	}

	public void softDelete(Long deleterId) {
		assertNotDeleted();
		this.status = ProductStatus.INACTIVE;
		markDeleted(deleterId);
	}

	public void validateHardDeletable() {
		assertNotDeleted();
		validate(status == ProductStatus.DRAFT, ErrorCode.PRODUCT_HARD_DELETE_NOT_ALLOWED);
	}


	private String validateName(String name) {
		validate(Strings.isBlank(name), ErrorCode.PRODUCT_NAME_REQUIRED);
		validate(name.length() <= 200, ErrorCode.PRODUCT_NAME_TOO_LONG);
		return name;
	}

	private int validatePrice(int price) {
		validate(price >= 0, ErrorCode.PRODUCT_PRICE_BELOW_MINIMUM);
		return price;
	}

	private void changeStatus(ProductStatus target) {
		assertNotDeleted();
		validate(status.canChangeTo(target), ErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
		this.status = target;
	}

	private void assertNotDeleted() {
		if (deleted) throw new CustomException(ErrorCode.PRODUCT_ALREADY_DELETED);
	}
}
