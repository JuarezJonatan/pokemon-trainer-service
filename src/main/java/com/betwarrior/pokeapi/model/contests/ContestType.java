package com.betwarrior.pokeapi.model.contests;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.berries.BerryFlavor;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ContestType(
		int id,
		String name,
		NamedRef<BerryFlavor> berryFlavor,
		List<ContestName> names) implements NamedResource {

	public ContestType {
		names = Lists.nullSafeCopy(names);
	}

}
