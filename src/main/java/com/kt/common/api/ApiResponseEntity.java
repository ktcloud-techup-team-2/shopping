package com.kt.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * - Spring ResponseEntity를 확장, 모든 응답 바디를 ApiResponse 형태로 감싸는 공통 래퍼 클래스
 * - 각 정적 팩토리 메서드를 통해 상태 코드, 헤더, 페이지네이션 정보를 일관된 형태로 생성 가능
 *
 * @param <T> 응답 데이터의 타입
 * @return 공통 응답 스키마(ApiResponse)를 포함한 ApiResponseEntity 인스턴스
 * @see ResponseEntity
 * @see ApiResponse
 * @see PageBlock
 * @see SliceBlock
 */

public final class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {

	/* 생성자는 외부에서 직접 쓰지 못하게 캡슐화 */
	private ApiResponseEntity(ApiResponse<T> body, HttpStatus status) {
		super(body, status);
	}
	private ApiResponseEntity(ApiResponse<T> body, HttpStatus status, HttpHeaders headers) {
		super(body, headers, status);
	}

	/* ---------------------------- 단건/일반 응답 ---------------------------- */

	/** 200 OK + { "data": ... } */
	public static <T> ApiResponseEntity<T> success(T body) {
		return new ApiResponseEntity<>(ApiResponse.of(body), HttpStatus.OK);
	}

	/** 임의의 상태코드 + { "data": ... } */
	public static <T> ApiResponseEntity<T> withStatus(HttpStatus status, T body) {
		return new ApiResponseEntity<>(ApiResponse.of(body), status);
	}

	/** 임의의 상태코드 + 헤더 + { "data": ... } */
	public static <T> ApiResponseEntity<T> withStatus(
		HttpStatus status, T body, Consumer<HttpHeaders> headersConsumer
	) {
		HttpHeaders headers = new HttpHeaders();
		if (headersConsumer != null) headersConsumer.accept(headers);
		return new ApiResponseEntity<>(ApiResponse.of(body), status, headers);
	}

	/** 202 Accepted + { "data": ... } */
	public static <T> ApiResponseEntity<T> accepted(T body) {
		return new ApiResponseEntity<>(ApiResponse.of(body), HttpStatus.ACCEPTED);
	}

	/* ---------------------------- 생성/삭제 편의 ---------------------------- */

	/** 201 Created + { "data": ... }  (Location 없이 바디만) */
	public static <T> ApiResponseEntity<T> created(T body) {
		return new ApiResponseEntity<>(ApiResponse.of(body), HttpStatus.CREATED);
	}

	/** 204 No Content (바디 없음) */
	public static ApiResponseEntity<Void> empty() {
		return new ApiResponseEntity<>(null, HttpStatus.NO_CONTENT);
	}

	/* ---------------------------- 페이지네이션 ---------------------------- */

	/** 200 OK + { "data": [...], "page": {...} } */
	public static <T> ApiResponseEntity<List<T>> pageOf(Page<T> page) {
		return new ApiResponseEntity<>(
			ApiResponse.ofPage(page.getContent(), toPageBlock(page)),
			HttpStatus.OK
		);
	}

	/** 200 OK + { "data": [...], "slice": {...} } */
	public static <T> ApiResponseEntity<List<T>> sliceOf(Slice<T> slice) {
		return new ApiResponseEntity<>(
			ApiResponse.ofSlice(slice.getContent(), toSliceBlock(slice)),
			HttpStatus.OK
		);
	}

	/* ---------------------------- 내부 변환 유틸 ---------------------------- */

	private static <T> PageBlock toPageBlock(Page<T> page) {
		List<PageBlock.SortOrder> sorts = new ArrayList<>();
		for (Sort.Order o : page.getSort()) {
			sorts.add(new PageBlock.SortOrder(o.getProperty(), o.getDirection().name()));
		}
		return new PageBlock(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext(),
			page.hasPrevious(),
			sorts
		);
	}

	private static <T> SliceBlock toSliceBlock(Slice<T> slice) {
		return new SliceBlock(slice.getNumber(), slice.getSize(), slice.hasNext());
	}
}
