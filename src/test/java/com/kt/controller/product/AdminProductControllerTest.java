package com.kt.controller.product;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kt.common.api.ApiResponse;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AdminProductControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/admin/products";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Nested
	class 상품_생성_API {

		@Test
		void 성공() throws Exception {
			// given
			ProductRequest.Create request = new ProductRequest.Create(
				"테스트 상품명",
				"테스트 상품 설명",
				10_000,
				10
			);

			var responseBody = new ProductResponse.Create(1L);
			var docsResponse = ApiResponse.of(responseBody);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"admin-products-create", // identifier (스니펫 이름)
						"상품 등록", // summary (swagger summary)
						"관리자 상품 생성 API",  // description
						"Admin-Product", // tag
						request,
						docsResponse
					)
				);
		}
	}
}