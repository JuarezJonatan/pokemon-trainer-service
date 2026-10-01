package com.betwarrior.pokeapi.model.evolution;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EvolutionTrigger(
		int id,
		String name,
		List<Name> names,
		List<NamedRef<PokemonSpecies>> pokemonSpecies) implements NamedResource, Localized {

	public EvolutionTrigger {
		names = Lists.nullSafeCopy(names);
		pokemonSpecies = Lists.nullSafeCopy(pokemonSpecies);
	}

}
