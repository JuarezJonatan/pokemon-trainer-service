package com.betwarrior.pokeapi.model.species;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonShape(
		int id,
		String name,
		List<AwesomeName> awesomeNames,
		List<Name> names,
		List<NamedRef<PokemonSpecies>> pokemonSpecies) implements NamedResource, Localized {

	public PokemonShape {
		awesomeNames = Lists.nullSafeCopy(awesomeNames);
		names = Lists.nullSafeCopy(names);
		pokemonSpecies = Lists.nullSafeCopy(pokemonSpecies);
	}

}
