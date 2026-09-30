package com.betwarrior.pokestorage.domain;

import java.util.EnumMap;
import java.util.Map;

public record StatValues(int hp, int attack, int defense, int specialAttack, int specialDefense, int speed) {

	public int valueOf(Stat stat) {
		return switch (stat) {
			case HP -> hp;
			case ATTACK -> attack;
			case DEFENSE -> defense;
			case SPECIAL_ATTACK -> specialAttack;
			case SPECIAL_DEFENSE -> specialDefense;
			case SPEED -> speed;
		};
	}

	public int total() {
		return hp + attack + defense + specialAttack + specialDefense + speed;
	}

	public Map<Stat, Integer> asMap() {
		Map<Stat, Integer> values = new EnumMap<>(Stat.class);
		for (Stat stat : Stat.values()) {
			values.put(stat, valueOf(stat));
		}
		return values;
	}

	static StatValues from(Map<Stat, Integer> values) {
		return new StatValues(values.get(Stat.HP), values.get(Stat.ATTACK), values.get(Stat.DEFENSE),
				values.get(Stat.SPECIAL_ATTACK), values.get(Stat.SPECIAL_DEFENSE), values.get(Stat.SPEED));
	}

}
