package com.betwarrior.pokeapi.model.locations;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record LocationArea(
		int id,
		String name,
		Integer gameIndex,
		List<EncounterMethodRate> encounterMethodRates,
		NamedRef<Location> location,
		List<Name> names,
		List<PokemonEncounter> pokemonEncounters) implements NamedResource, Localized {

	public LocationArea {
		encounterMethodRates = Lists.nullSafeCopy(encounterMethodRates);
		names = Lists.nullSafeCopy(names);
		pokemonEncounters = Lists.nullSafeCopy(pokemonEncounters);
	}

}
