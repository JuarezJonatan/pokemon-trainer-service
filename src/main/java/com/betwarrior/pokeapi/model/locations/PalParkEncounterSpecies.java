package com.betwarrior.pokeapi.model.locations;

import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PalParkEncounterSpecies(
		Integer baseScore,
		Integer rate,
		NamedRef<PokemonSpecies> pokemonSpecies) {

}
