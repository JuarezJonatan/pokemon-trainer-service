package com.betwarrior.pokeapi.model.species;

import com.betwarrior.pokeapi.model.games.Pokedex;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonSpeciesDexEntry(
		Integer entryNumber,
		NamedRef<Pokedex> pokedex) {

}
