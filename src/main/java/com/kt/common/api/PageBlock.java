package com.kt.common.api;


import java.util.List;

public record PageBlock(
	int number,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext,
	boolean hasPrev,
	List<SortOrder> sort
) {
	public record SortOrder(String property, String direction) {}
}