package com.betwarrior.pokestorage.application.pagination;

import java.util.List;
import java.util.function.Function;

public record Page<T>(List<T> content, int page, int size, long totalElements) {

	public Page {
		content = List.copyOf(content);
	}

	public static <T> Page<T> of(List<T> content, PageRequest request, long totalElements) {
		return new Page<>(content, request.page(), request.size(), totalElements);
	}

	public long totalPages() {
		return (totalElements + size - 1) / size;
	}

	public <R> Page<R> map(Function<T, R> mapper) {
		return new Page<>(content.stream().map(mapper).toList(), page, size, totalElements);
	}

}
