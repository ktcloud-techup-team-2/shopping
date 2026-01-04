package com.kt.controller.wms;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.ErrorCode;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.dto.wms.InboundConfirmedRequest;
import com.kt.dto.wms.InboundConfirmedResponse;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.inventory.ProcessedInboundEventRepository;
import com.kt.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class WmsInboundControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/wms/inbounds/confirmed";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private ProcessedInboundEventRepository processedInboundEventRepository;

	@Autowired
	private RedissonClient redissonClient;

	@BeforeEach
	void clearProcessedEvents() {
		processedInboundEventRepository.deleteAll();
	}

	@Nested
	class 입고_확정_API {

		@Test
		void 성공_문서화() throws Exception {
			stubLockSuccess();
			Product product = createProductWithInventory();

			InboundConfirmedRequest request = new InboundConfirmedRequest(
				"event-1001",
				product.getId(),
				5L
			);
			var docsResponse = ApiResponse.of(InboundConfirmedResponse.of(
				request.eventId(),
				request.productId(),
				request.quantity(),
				LocalDateTime.of(2024, 1, 1, 10, 0)
			));

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"wms-inbounds-confirmed",
						"입고 확정 수신",
						"WMS 입고 확정 이벤트를 수신합니다.",
						"WMS-Inbound",
						request,
						docsResponse
					)
				);
		}

		@Test
		void 동일_eventId_재요청시_재고가_한번만_증가한다() throws Exception {
			stubLockSuccess();
			Product product = createProductWithInventory();

			InboundConfirmedRequest request = new InboundConfirmedRequest(
				"event-dup",
				product.getId(),
				3L
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isOk());

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isOk());

			Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
			assertThat(inventory.getPhysicalStockTotal()).isEqualTo(3L);
		}

		@Test
		void 서로_다른_eventId는_누적_증가한다() throws Exception {
			stubLockSuccess();
			Product product = createProductWithInventory();

			InboundConfirmedRequest first = new InboundConfirmedRequest(
				"event-1",
				product.getId(),
				4L
			);
			InboundConfirmedRequest second = new InboundConfirmedRequest(
				"event-2",
				product.getId(),
				6L
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						first,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isOk());

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						second,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isOk());

			Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
			assertThat(inventory.getPhysicalStockTotal()).isEqualTo(10L);
		}

		@Test
		void 상품이_없으면_실패하고_이벤트가_발행되지_않는다() throws Exception {
			InboundConfirmedRequest request = new InboundConfirmedRequest(
				"event-missing-product",
				999999L,
				2L
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value(ErrorCode.WMS_INBOUND_PRODUCT_NOT_FOUND.name()))
			.andDo(
					errorDocs(
						"wms-inbounds-confirmed-product-not-found",
						"입고 확정 실패 - 상품 없음",
						"존재하지 않는 상품으로 입고 확정 요청 시 실패합니다.",
						request
					)
				);

			assertThat(processedInboundEventRepository.count()).isZero();
		}

		@Test
		void 수량이_0이하면_실패한다() throws Exception {
			InboundConfirmedRequest request = new InboundConfirmedRequest(
				"event-invalid-quantity",
				1L,
				0L
			);

			mockMvc.perform(
					restDocsFactory.createRequest(
						DEFAULT_URL,
						request,
						HttpMethod.POST,
						objectMapper
					).with(jwtUser())
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value(ErrorCode.WMS_INBOUND_EVENT_QUANTITY_INVALID.name()))
				.andDo(
					errorDocs(
						"wms-inbounds-confirmed-quantity-invalid",
						"입고 확정 실패 - 수량 오류",
						"입고 수량이 0 이하이면 실패합니다.",
						request
					)
				);

			assertThat(processedInboundEventRepository.count()).isZero();
		}
	}

	private void stubLockSuccess() throws Exception {
		RLock lock = mock(RLock.class);
		when(redissonClient.getLock(anyString())).thenReturn(lock);
		when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
	}

	private Product createProductWithInventory() {
		Product product = productRepository.save(Product.create(
			"입고 테스트 상품",
			"입고 테스트 설명",
			10_000,
			PetType.DOG
		));
		Inventory inventory = Inventory.initialize(product);
		inventoryRepository.save(inventory);
		return product;
	}

	private RestDocumentationResultHandler errorDocs(
		String identifier,
		String summary,
		String description,
		InboundConfirmedRequest request
	) {
		FieldDescriptor[] responseFields = new FieldDescriptor[] {
			fieldWithPath("type").type(JsonFieldType.STRING).optional().description("에러 타입"),
			fieldWithPath("title").type(JsonFieldType.STRING).optional().description("에러 코드"),
			fieldWithPath("status").type(JsonFieldType.NUMBER).optional().description("HTTP 상태 코드"),
			fieldWithPath("detail").type(JsonFieldType.STRING).optional().description("에러 메시지"),
			fieldWithPath("instance").type(JsonFieldType.STRING).optional().description("에러 인스턴스")
		};

		ResourceSnippetParameters resource = ResourceSnippetParameters.builder()
			.tag("WMS-Inbound")
			.summary(summary)
			.description(description)
			.requestSchema(Schema.schema(request.getClass().getSimpleName()))
			.responseSchema(Schema.schema("ProblemDetail"))
			.requestFields(restDocsFactory.getFields(request))
			.responseFields(responseFields)
			.build();

		return MockMvcRestDocumentationWrapper.document(
			identifier,
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(resource)
		);
	}
}