package com.betwarrior.pokeapi.model.berries;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.contests.ContestType;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record BerryFlavor(
		int id,
		String name,
		List<FlavorBerryMap> berries,
		NamedRef<ContestType> contestType,
		List<Name> names) implements NamedResource, Localized {

	public BerryFlavor {
		berries = Lists.nullSafeCopy(berries);
		names = Lists.nullSafeCopy(names);
	}

}
