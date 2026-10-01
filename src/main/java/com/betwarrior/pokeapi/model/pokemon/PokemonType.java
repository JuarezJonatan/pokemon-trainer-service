package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonType(
		int slot,
		NamedRef<Type> type) {

}
