package com.betwarrior.pokeapi.model.locations;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;

public record PalParkArea(
		int id,
		String name,
		List<Name> names,
		List<PalParkEncounterSpecies> pokemonEncounters) implements NamedResource, Localized {

	public PalParkArea {
		names = Lists.nullSafeCopy(names);
		pokemonEncounters = Lists.nullSafeCopy(pokemonEncounters);
	}

}
