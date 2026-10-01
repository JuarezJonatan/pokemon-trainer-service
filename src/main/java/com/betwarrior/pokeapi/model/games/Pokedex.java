package com.betwarrior.pokeapi.model.games;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.locations.Region;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Pokedex(
		int id,
		String name,
		boolean isMainSeries,
		List<Description> descriptions,
		List<Name> names,
		List<PokemonEntry> pokemonEntries,
		NamedRef<Region> region,
		List<NamedRef<VersionGroup>> versionGroups) implements NamedResource, Localized {

	public Pokedex {
		descriptions = Lists.nullSafeCopy(descriptions);
		names = Lists.nullSafeCopy(names);
		pokemonEntries = Lists.nullSafeCopy(pokemonEntries);
		versionGroups = Lists.nullSafeCopy(versionGroups);
	}

}
