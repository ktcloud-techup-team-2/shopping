package com.kt.domain.product;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProductStatusTest {
	@ParameterizedTest(name = "{0}에서_{1}(으)로_전이_가능한지_확인한다")
	@MethodSource("상태_전이_케이스")
	void canChangeTo_상태_전이_규칙을_만족한다(ProductStatus from, ProductStatus to, boolean expected) {
		// when
		boolean result = from.canChangeTo(to);

		// then
		assertThat(result)
			.as("%s -> %s 전이 가능 여부", from, to)
			.isEqualTo(expected);
	}

	private static Stream<Arguments> 상태_전이_케이스() {
		return Stream.of(
			// 자기 자신 → false
			Arguments.of(ProductStatus.DRAFT, ProductStatus.DRAFT, false),
			Arguments.of(ProductStatus.ACTIVE, ProductStatus.ACTIVE, false),
			Arguments.of(ProductStatus.INACTIVE, ProductStatus.INACTIVE, false),
			Arguments.of(ProductStatus.SOLD_OUT, ProductStatus.SOLD_OUT, false),

			// DRAFT, SOLD_OUT -> ACTIVE, INACTIVE
			Arguments.of(ProductStatus.DRAFT, ProductStatus.ACTIVE, true),
			Arguments.of(ProductStatus.DRAFT, ProductStatus.INACTIVE, true),
			Arguments.of(ProductStatus.SOLD_OUT, ProductStatus.ACTIVE, true),
			Arguments.of(ProductStatus.SOLD_OUT, ProductStatus.INACTIVE, true),

			// ACTIVE -> SOLD_OUT, INACTIVE
			Arguments.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT, true),
			Arguments.of(ProductStatus.ACTIVE, ProductStatus.INACTIVE, true),

			// INACTIVE -> ACTIVE, SOLD_OUT
			Arguments.of(ProductStatus.INACTIVE, ProductStatus.ACTIVE, true),
			Arguments.of(ProductStatus.INACTIVE, ProductStatus.SOLD_OUT, true),

			// 허용되지 않는 전이 명시적으로 한 번 박아두기 (DRAFT -> SOLD_OUT)
			Arguments.of(ProductStatus.DRAFT, ProductStatus.SOLD_OUT, false)
		);
	}
}