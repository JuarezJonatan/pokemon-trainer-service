package com.betwarrior.pokestorage.application.pagination;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

/**
 * A zero-based page of at most {@link #MAX_SIZE} elements.
 */
public record PageRequest(int page, int size) {

	public static final int MAX_SIZE = 100;

	public PageRequest {
		if (page < 0 || size < 1 || size > MAX_SIZE) {
			throw new InvalidValueException("page must be >= 0 and size between 1 and %d".formatted(MAX_SIZE));
		}
	}

	public int offset() {
		return page * size;
	}

}
