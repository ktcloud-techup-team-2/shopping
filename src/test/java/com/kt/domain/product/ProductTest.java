package com.kt.domain.product;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.PetType;

class ProductTest {
	@Test
	void 상품_생성에_성공한다() {
		// given
		String name = "테스트 상품명";
		String description = "테스트 상품 설명";
		int price = 100_000;
		PetType petType = PetType.DOG;

		// when
		Product product = Product.create(name, description, price, petType);

		// then
		assertThat(product.getName()).isEqualTo(name);
		assertThat(product.getDescription()).isEqualTo(description);
		assertThat(product.getPrice()).isEqualTo(price);

		// 최초 상태는 항상 DRAFT
		assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 상품명_null_또는_빈문자열이면_예외가_발생한다(String name) {
		// when & then
		assertThatThrownBy(() -> Product.create(
			name,
			"설명",
			10_000,
			PetType.DOG
		))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NAME_REQUIRED);
	}

	@Test
	void 상품명이_200자를_초과하면_예외가_발생한다() {
		// given
		String name = "a".repeat(201);

		// when & then
		assertThatThrownBy(() -> Product.create(
			name,
			"설명",
			10_000,
			PetType.DOG
		))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NAME_TOO_LONG);
	}

	@Test
	void 가격이_음수면_예외가_발생한다() {
		// when & then
		assertThatThrownBy(() -> Product.create(
			"테스트 상품명",
			"설명",
			-1,
			PetType.DOG
		))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_PRICE_BELOW_MINIMUM);
	}

	@Test
	void 가격과_재고는_0이어도_정상_생성된다() {
		// when
		Product product = Product.create(
			"테스트 상품명",
			"설명",
			0,
			PetType.DOG
		);

		// then
		assertThat(product.getPrice()).isZero();
		assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
	}

	@Test
	void create_requiresPetType() {
		assertThatThrownBy(() -> Product.create(
			"상품",
			"설명",
			1000,
			null
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_requiresPetType() {
		Product product = Product.create("상품", "설명", 1000, PetType.DOG);

		assertThatThrownBy(() -> product.update("상품", "설명", 2000, null))
			.isInstanceOf(CustomException.class);
	}
}