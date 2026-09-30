package com.betwarrior.pokestorage.domain.pokemon;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

public record Level(int value) {

	public static final int MIN = 1;
	public static final int MAX = 100;

	public Level {
		if (value < MIN || value > MAX) {
			throw new InvalidValueException("Level must be between %d and %d but was %d".formatted(MIN, MAX, value));
		}
	}

}
