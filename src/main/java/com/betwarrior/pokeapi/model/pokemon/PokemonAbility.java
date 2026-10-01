package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.model.abilities.Ability;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonAbility(
		boolean isHidden,
		int slot,
		NamedRef<Ability> ability) {

}
