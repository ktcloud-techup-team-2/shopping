package com.kt.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
	DRAFT("임시저장"),
	ACTIVE("활성"),
	INACTIVE("비활성"),
	SOLD_OUT("품절");

	private final String description;

	public boolean canChangeTo(ProductStatus target) {
		if (this == target) return false;

		return switch (this) {
			case DRAFT, SOLD_OUT -> target == ACTIVE || target == INACTIVE;
			case ACTIVE   -> target == SOLD_OUT || target == INACTIVE;
			case INACTIVE -> target == ACTIVE || target == SOLD_OUT;
		};
	}

	@Override
	public String toString() {
		return name();
	}
}
