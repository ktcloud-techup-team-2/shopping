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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Component
public class RestDocsFactory {

	// ========== Request Builder ==========

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

	// ========== Success Resource (단건) ==========

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

	// ========== Field 자동 생성 ==========

	public <T> FieldDescriptor[] getFields(T dto) {
		List<FieldDescriptor> fields = new ArrayList<>();
		generateFieldDescriptors(dto, "", fields);
		return fields.toArray(new FieldDescriptor[0]);
	}

	private <T> void generateFieldDescriptors(T dto, String pathPrefix, List<FieldDescriptor> fields) {
		if (dto == null || isSimpleType(dto)) {
			return;
		}

		Field[] declaredFields = dto.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			String fieldPath = pathPrefix + field.getName();
			Object fieldValue = getFieldValue(dto, field);
			JsonFieldType fieldType = determineFieldType(field.getType(), fieldValue);

			FieldDescriptor descriptor = PayloadDocumentation.fieldWithPath(fieldPath)
				.type(fieldType)
				.description(field.getName())
				.optional(); // ★ 여기! page, slice 같은 필드가 JSON에 없어도 허용

			fields.add(descriptor);

			// 리스트 타입 처리
			if (fieldType == JsonFieldType.ARRAY && fieldValue instanceof List<?> list) {
				if (!list.isEmpty()) {
					Object firstElement = list.getFirst();
					generateFieldDescriptors(firstElement, fieldPath + "[].", fields);
				}
			}

			// 오브젝트 타입 처리
			if (fieldType == JsonFieldType.OBJECT && fieldValue != null) {
				generateFieldDescriptors(fieldValue, fieldPath + ".", fields);
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

    private boolean isSimpleType(Object dto) {
        if (dto == null) return true;

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

	private JsonFieldType determineFieldType(Class<?> fieldType, Object fieldValue) {
		if (fieldValue instanceof List<?>) {
			return JsonFieldType.ARRAY;
		}
		if (fieldType == String.class || fieldType.isEnum()) {
			return JsonFieldType.STRING;
		} else if (Boolean.class.isAssignableFrom(fieldType) || fieldType == boolean.class) {
			return JsonFieldType.BOOLEAN;
		} else if (Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive()) {
			return JsonFieldType.NUMBER;
		} else if (List.class.isAssignableFrom(fieldType)) {
			return JsonFieldType.ARRAY;
		} else if (fieldType == LocalDate.class
                || fieldType == LocalDateTime.class
                || fieldType == LocalTime.class) {
            return JsonFieldType.STRING;
        } else {
			return JsonFieldType.OBJECT;
		}
	}
}
