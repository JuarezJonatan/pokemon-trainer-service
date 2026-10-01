package com.betwarrior.pokeapi.model.games;

import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonEntry(
		Integer entryNumber,
		NamedRef<PokemonSpecies> pokemonSpecies) {

}
