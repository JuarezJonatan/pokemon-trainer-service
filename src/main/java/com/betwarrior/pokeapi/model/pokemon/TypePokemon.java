package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.ref.NamedRef;

public record TypePokemon(
		int slot,
		NamedRef<Pokemon> pokemon) {

}
