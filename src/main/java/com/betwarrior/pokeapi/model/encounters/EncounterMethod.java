package com.betwarrior.pokeapi.model.encounters;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;

public record EncounterMethod(
		int id,
		String name,
		int order,
		List<Name> names) implements NamedResource, Localized {

	public EncounterMethod {
		names = Lists.nullSafeCopy(names);
	}

}
