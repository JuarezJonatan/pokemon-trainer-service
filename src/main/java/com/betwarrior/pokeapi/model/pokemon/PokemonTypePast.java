package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonTypePast(
		NamedRef<Generation> generation,
		List<PokemonType> types) {

	public PokemonTypePast {
		types = Lists.nullSafeCopy(types);
	}

}
