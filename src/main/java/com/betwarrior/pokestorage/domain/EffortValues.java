package com.betwarrior.pokestorage.domain;

public record EffortValues(StatValues values) {

	public static final int MAX_PER_STAT = 252;
	public static final int MAX_TOTAL = 510;

	public static EffortValues none() {
		return new EffortValues(new StatValues(0, 0, 0, 0, 0, 0));
	}

	public EffortValues {
		for (Stat stat : Stat.values()) {
			int value = values.valueOf(stat);
			if (value < 0 || value > MAX_PER_STAT) {
				throw new InvalidValueException(
						"EV for %s must be between 0 and %d but was %d".formatted(stat, MAX_PER_STAT, value));
			}
		}
		if (values.total() > MAX_TOTAL) {
			throw new InvalidValueException(
					"EVs must add up to at most %d but were %d".formatted(MAX_TOTAL, values.total()));
		}
	}

	public int of(Stat stat) {
		return values.valueOf(stat);
	}

}
