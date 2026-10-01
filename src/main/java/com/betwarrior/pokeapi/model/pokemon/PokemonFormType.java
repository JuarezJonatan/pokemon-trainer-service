package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonFormType(
		int slot,
		NamedRef<Type> type) {

}
