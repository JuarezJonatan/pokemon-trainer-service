package com.betwarrior.pokeapi.model.species;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.ref.NamedRef;

public record GrowthRate(
		int id,
		String name,
		String formula,
		List<Description> descriptions,
		List<GrowthRateExperienceLevel> levels,
		List<NamedRef<PokemonSpecies>> pokemonSpecies) implements NamedResource {

	public GrowthRate {
		descriptions = Lists.nullSafeCopy(descriptions);
		levels = Lists.nullSafeCopy(levels);
		pokemonSpecies = Lists.nullSafeCopy(pokemonSpecies);
	}

}
