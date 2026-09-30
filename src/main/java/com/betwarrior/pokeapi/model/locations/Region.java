package com.betwarrior.pokeapi.model.locations;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.model.games.Pokedex;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Region(
		int id,
		List<NamedRef<Location>> locations,
		String name,
		List<Name> names,
		NamedRef<Generation> mainGeneration,
		List<NamedRef<Pokedex>> pokedexes,
		List<NamedRef<VersionGroup>> versionGroups) implements NamedResource, Localized {

	public Region {
		locations = Lists.nullSafeCopy(locations);
		names = Lists.nullSafeCopy(names);
		pokedexes = Lists.nullSafeCopy(pokedexes);
		versionGroups = Lists.nullSafeCopy(versionGroups);
	}

}
