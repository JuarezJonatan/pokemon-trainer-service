package com.betwarrior.pokeapi.model.species;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Gender(
		int id,
		String name,
		List<PokemonSpeciesGender> pokemonSpeciesDetails,
		List<NamedRef<PokemonSpecies>> requiredForEvolution) implements NamedResource {

	public Gender {
		pokemonSpeciesDetails = Lists.nullSafeCopy(pokemonSpeciesDetails);
		requiredForEvolution = Lists.nullSafeCopy(requiredForEvolution);
	}

}
