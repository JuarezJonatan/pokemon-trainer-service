package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonAbilityPast(
		NamedRef<Generation> generation,
		List<PokemonAbility> abilities) {

	public PokemonAbilityPast {
		abilities = Lists.nullSafeCopy(abilities);
	}

}
