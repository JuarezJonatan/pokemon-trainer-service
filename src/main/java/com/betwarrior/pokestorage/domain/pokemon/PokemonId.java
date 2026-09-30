package com.betwarrior.pokestorage.domain.pokemon;

import java.util.Objects;
import java.util.UUID;

public record PokemonId(UUID value) {

	public PokemonId {
		Objects.requireNonNull(value, "Pokemon id is required");
	}

	public static PokemonId random() {
		return new PokemonId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
