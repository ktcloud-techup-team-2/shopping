package com.kt.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ApiResponse<T> {
	private final T data;
	private final PageBlock page;
	private final SliceBlock slice;

	public static <T> ApiResponse<T> of(T data) {
		return new ApiResponse<>(data, null, null);
	}
	public static <T> ApiResponse<T> ofPage(T data, PageBlock page) {
		return new ApiResponse<>(data, page, null);
	}
	public static <T> ApiResponse<T> ofSlice(T data, SliceBlock slice) {
		return new ApiResponse<>(data, null, slice);
	}
}