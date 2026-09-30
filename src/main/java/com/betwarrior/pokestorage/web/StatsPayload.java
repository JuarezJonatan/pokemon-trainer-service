package com.betwarrior.pokestorage.web;

import com.betwarrior.pokestorage.domain.StatValues;

import jakarta.validation.constraints.NotNull;

public record StatsPayload(
		@NotNull Integer hp,
		@NotNull Integer attack,
		@NotNull Integer defense,
		@NotNull Integer specialAttack,
		@NotNull Integer specialDefense,
		@NotNull Integer speed) {

	static StatsPayload from(StatValues values) {
		return new StatsPayload(values.hp(), values.attack(), values.defense(), values.specialAttack(),
				values.specialDefense(), values.speed());
	}

	StatValues toStatValues() {
		return new StatValues(hp, attack, defense, specialAttack, specialDefense, speed);
	}

}
