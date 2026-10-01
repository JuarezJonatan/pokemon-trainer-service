package com.betwarrior.pokeapi.model.species;

import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonSpeciesGender(
		Integer rate,
		NamedRef<PokemonSpecies> pokemonSpecies) {

}
