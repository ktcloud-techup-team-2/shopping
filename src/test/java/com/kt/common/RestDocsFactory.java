package com.kt.common;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * RestDocs + restdocs-api-spec 연동을 위한 공통 팩토리 클래스
 * - MockMvc 요청 빌더
 * - 요청/응답 스키마 + 필드 자동 생성
 */
@Component
public class RestDocsFactory {

	/**
	 * HTTP 메서드, URL, 바디에 맞는 MockMvc 요청 생성
	 */
	public MockHttpServletRequestBuilder createRequest(
		String url,
		Object requestDto,
		HttpMethod method,
		ObjectMapper objectMapper,
		Object... pathParams
	) throws Exception {

		String content = requestDto != null
			? objectMapper.writeValueAsString(requestDto)
			: "";

		return buildRequest(url, content, method, pathParams);
	}

	/**
	 * GET + QueryString 전용 요청 빌더
	 * - dto 를 쿼리 파라미터로 변환해서 붙여줌
	 * - Pageable 도 같이 받으면 page/size/sort 도 자동 추가
	 */
	public MockHttpServletRequestBuilder createParamRequest(
		String url,
		Object queryDto,                 // 검색 조건 DTO (ex. SearchCond)
		Pageable pageable,              // PageRequest.of(...)
		ObjectMapper objectMapper,
		Object... pathParams
	) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		// 검색 조건 DTO → 쿼리 스트링
		if (queryDto != null) {
			params.addAll(
				MultiValueMapConverter.convert(objectMapper, queryDto)
			);
		}

		// 2) Pageable → page / size / sort
		if (pageable != null) {
			params.add("page", String.valueOf(pageable.getPageNumber()));
			params.add("size", String.valueOf(pageable.getPageSize()));

			pageable.getSort().forEach(order -> {
				String sortParam = order.getProperty() + "," + order.getDirection().name();
				params.add("sort", sortParam); // ex) sort=createdAt,DESC
			});
		}

		// GET 요청 빌더 생성
		return RestDocumentationRequestBuilders.get(url, pathParams)
			.params(params)
			.accept(MediaType.APPLICATION_JSON);
	}

	private MockHttpServletRequestBuilder buildRequest(
		String url,
		String content,
		HttpMethod method,
		Object... pathParams
	) {
		return switch (method.name()) {
			case "POST" -> RestDocumentationRequestBuilders.post(url, pathParams)
				.contentType(MediaType.APPLICATION_JSON)
				.content(content)
				.accept(MediaType.APPLICATION_JSON);

			case "PUT" -> RestDocumentationRequestBuilders.put(url, pathParams)
				.contentType(MediaType.APPLICATION_JSON)
				.content(content)
				.accept(MediaType.APPLICATION_JSON);

			case "PATCH" -> RestDocumentationRequestBuilders.patch(url, pathParams)
				.contentType(MediaType.APPLICATION_JSON)
				.content(content)
				.accept(MediaType.APPLICATION_JSON);

			case "GET" -> RestDocumentationRequestBuilders.get(url, pathParams)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);

			case "DELETE" -> RestDocumentationRequestBuilders.delete(url, pathParams)
				.contentType(MediaType.APPLICATION_JSON)
				.content(content)
				.accept(MediaType.APPLICATION_JSON);

			default -> throw new IllegalArgumentException("Invalid HTTP method: " + method);
		};
	}

	// ========================================================================
	// Success Resource (단건)
	// ========================================================================

	/**
	 * 성공 케이스용 공통 문서 생성
	 *
	 * @param identifier   스니펫 이름 (ex. "admin-products-create")
	 * @param summary      swagger summary
	 * @param description  swagger description
	 * @param tag          swagger tag
	 * @param requestDto   요청 DTO (GET 등 body 없으면 null)
	 * @param responseDto  응답 DTO (응답 body 없으면 null)
	 */
	public <T, R> RestDocumentationResultHandler success(
		String identifier,
		String summary,
		String description,
		String tag,
		T requestDto,
		R responseDto
	) {

		String requestSchemaName = requestDto != null ? requestDto.getClass().getSimpleName() : null;
		String responseSchemaName = responseDto != null ? responseDto.getClass().getSimpleName() : null;

		// 요청 body 없는 경우 (GET 등)
		if (requestDto == null) {
			return MockMvcRestDocumentationWrapper.document(
				identifier,
				preprocessResponse(prettyPrint()),
				resource(
					ResourceSnippetParameters.builder()
						.tag(tag)
						.summary(summary)
						.description(description)
						.responseSchema(responseSchemaName != null ? Schema.schema(responseSchemaName) : null)
						.responseFields(responseDto != null ? getFields(responseDto) : new FieldDescriptor[] {})
						.build()
				)
			);
		}

		// 일반적인 요청/응답 둘 다 있는 경우
		return MockMvcRestDocumentationWrapper.document(
			identifier,
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(
				ResourceSnippetParameters.builder()
					.tag(tag)
					.summary(summary)
					.description(description)
					.requestSchema(Schema.schema(requestSchemaName))
					.responseSchema(responseSchemaName != null ? Schema.schema(responseSchemaName) : null)
					.requestFields(getFields(requestDto))
					.responseFields(responseDto != null ? getFields(responseDto) : new FieldDescriptor[] {})
					.build()
			)
		);
	}

	// ========================================================================
	// Field 자동 생성
	// ========================================================================

	/**
	 * DTO 구조를 기반으로 JsonFieldDescriptor 배열을 생성
	 */
	public <T> FieldDescriptor[] getFields(T dto) {
		List<FieldDescriptor> fields = new ArrayList<>();
		generateFieldDescriptors(dto, "", fields);
		return fields.toArray(new FieldDescriptor[0]);
	}

	/**
	 * 리플렉션으로 필드를 순회하며 필드/서브필드를 자동 문서화
	 * - Map 타입(nodesById 등)은 subsection으로만 문서화하고 내부 키는 스키마에서 제외
	 * - page, slice 같은 필드는 optional 로 선언하여 JSON에 없어도 에러가 나지 않도록 처리
	 */
	private <T> void generateFieldDescriptors(T dto, String pathPrefix, List<FieldDescriptor> fields) {
		if (dto == null || isSimpleType(dto)) {
			return;
		}

		Field[] declaredFields = dto.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			String fieldPath = pathPrefix + field.getName();
			Object fieldValue = getFieldValue(dto, field);
			Class<?> fieldTypeClass = field.getType();

			// Map 타입(nodesById 등)은 동적 키를 가지므로 subsection으로만 문서화
			if (fieldValue instanceof Map<?, ?> || Map.class.isAssignableFrom(fieldTypeClass)) {
				FieldDescriptor descriptor = PayloadDocumentation
					.subsectionWithPath(fieldPath)
					.type(JsonFieldType.OBJECT)
					.description(field.getName())
					.optional();

				fields.add(descriptor);
				continue;
			}

			JsonFieldType fieldType = determineFieldType(fieldTypeClass, fieldValue);

			FieldDescriptor descriptor = PayloadDocumentation.fieldWithPath(fieldPath)
				.type(fieldType)
				.description(field.getName())
				.optional(); // page, slice 같은 필드가 JSON에 없어도 허용

			fields.add(descriptor);

			// 리스트 타입 처리
			if (fieldType == JsonFieldType.ARRAY && fieldValue instanceof List<?> list && !list.isEmpty()) {
				Object firstElement = list.get(0);
				generateFieldDescriptors(firstElement, fieldPath + "[].", fields);
			}

			// 오브젝트 타입 처리 (Map은 위에서 처리했으니 제외)
			if (fieldType == JsonFieldType.OBJECT && fieldValue != null) {
				// java.* 타입(예: LocalDate, List, Map 등)은 isSimpleType에서 filter
				if (!isSimpleType(fieldValue)) {
					generateFieldDescriptors(fieldValue, fieldPath + ".", fields);
				}
			}
		}
	}

	private <T> Object getFieldValue(T dto, Field field) {
		try {
			return field.get(dto);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to access field: " + field.getName(), e);
		}
	}

	/**
	 * 더 이상 필드를 파고들지 않을 단순 타입 여부 판단
	 * - 원시 타입, enum
	 * - java.* 패키지 타입들 (List, Map, LocalDate 등)
	 * - String, Number, Boolean
	 */
	private boolean isSimpleType(Object dto) {
		if (dto == null) {
			return true;
		}

		Class<?> type = dto.getClass();

		// 원시 타입, enum
		if (type.isPrimitive() || type.isEnum()) {
			return true;
		}

		// JDK 타입들(java.*)은 더 이상 안 파고 든다 (Class, Module, Map, List, LocalDate 등)
		Package pkg = type.getPackage();
		if (pkg != null && pkg.getName().startsWith("java.")) {
			return true;
		}

		// 기본 wrapper / 문자열
		return dto instanceof String
			|| dto instanceof Number
			|| dto instanceof Boolean;
	}

	/**
	 * 필드 타입을 JsonFieldType으로 매핑
	 */
	private JsonFieldType determineFieldType(Class<?> fieldType, Object fieldValue) {
		if (fieldValue instanceof List<?>) {
			return JsonFieldType.ARRAY;
		}
		if (Map.class.isAssignableFrom(fieldType)) {
			return JsonFieldType.OBJECT;
		}
		if (fieldType == String.class || fieldType.isEnum()) {
			return JsonFieldType.STRING;
		}
		if (Boolean.class.isAssignableFrom(fieldType) || fieldType == boolean.class) {
			return JsonFieldType.BOOLEAN;
		}
		if (Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive()) {
			return JsonFieldType.NUMBER;
		}
		if (List.class.isAssignableFrom(fieldType)) {
			return JsonFieldType.ARRAY;
		}
		if (fieldType == LocalDate.class
			|| fieldType == LocalDateTime.class
			|| fieldType == LocalTime.class) {
			return JsonFieldType.STRING;
		}
		return JsonFieldType.OBJECT;
	}
}