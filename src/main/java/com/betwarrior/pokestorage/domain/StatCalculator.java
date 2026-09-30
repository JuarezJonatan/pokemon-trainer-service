package com.betwarrior.pokestorage.domain;

import java.util.EnumMap;
import java.util.Map;

/**
 * Main-series stat formula (Generation III onwards).
 */
final class StatCalculator {

	private StatCalculator() {
	}

	static StatValues calculate(StatValues base, Level level, IndividualValues ivs, EffortValues evs, Nature nature) {
		Map<Stat, Integer> stats = new EnumMap<>(Stat.class);
		for (Stat stat : Stat.values()) {
			int core = (2 * base.valueOf(stat) + ivs.of(stat) + evs.of(stat) / 4) * level.value() / 100;
			int value = stat == Stat.HP
					? core + level.value() + 10
					: (int) Math.floor((core + 5) * nature.modifierFor(stat));
			stats.put(stat, value);
		}
		return StatValues.from(stats);
	}

}
