package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonStatPast(
		NamedRef<Generation> generation,
		List<PokemonStat> stats) {

	public PokemonStatPast {
		stats = Lists.nullSafeCopy(stats);
	}

}
