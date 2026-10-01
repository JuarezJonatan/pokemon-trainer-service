package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;

public record Currency(
		int id,
		String name,
		List<Name> names) implements NamedResource, Localized {

	public Currency {
		names = Lists.nullSafeCopy(names);
	}

}
