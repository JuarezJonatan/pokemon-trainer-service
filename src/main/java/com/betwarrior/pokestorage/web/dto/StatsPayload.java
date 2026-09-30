package com.betwarrior.pokestorage.web.dto;

import com.betwarrior.pokestorage.domain.stats.StatValues;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Stats", description = "A value per stat. Its meaning (IVs, EVs, base or calculated stats) depends on the field")
public record StatsPayload(
		@Schema(description = "HP", example = "31") @NotNull Integer hp,
		@Schema(description = "Attack", example = "31") @NotNull Integer attack,
		@Schema(description = "Defense", example = "31") @NotNull Integer defense,
		@Schema(description = "Special Attack", example = "31") @NotNull Integer specialAttack,
		@Schema(description = "Special Defense", example = "31") @NotNull Integer specialDefense,
		@Schema(description = "Speed", example = "31") @NotNull Integer speed) {

	static StatsPayload from(StatValues values) {
		return new StatsPayload(values.hp(), values.attack(), values.defense(), values.specialAttack(),
				values.specialDefense(), values.speed());
	}

	StatValues toStatValues() {
		return new StatValues(hp, attack, defense, specialAttack, specialDefense, speed);
	}

}
