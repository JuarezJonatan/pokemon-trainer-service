package com.betwarrior.pokeapi.model.abilities;

import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.ref.NamedRef;

public record AbilityPokemon(
		boolean isHidden,
		int slot,
		NamedRef<Pokemon> pokemon) {

}
