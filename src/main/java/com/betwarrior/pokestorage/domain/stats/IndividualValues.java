package com.betwarrior.pokestorage.domain.stats;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

public record IndividualValues(StatValues values) {

	public static final int MIN = 0;
	public static final int MAX = 31;

	public IndividualValues {
		for (Stat stat : Stat.values()) {
			int value = values.valueOf(stat);
			if (value < MIN || value > MAX) {
				throw new InvalidValueException(
						"IV for %s must be between %d and %d but was %d".formatted(stat, MIN, MAX, value));
			}
		}
	}

	public int of(Stat stat) {
		return values.valueOf(stat);
	}

}
