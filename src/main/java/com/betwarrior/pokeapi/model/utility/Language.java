package com.betwarrior.pokeapi.model.utility;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;

public record Language(
		int id,
		String name,
		boolean official,
		List<Name> names,
		String iso639,
		String iso3166) implements NamedResource, Localized {

	public Language {
		names = Lists.nullSafeCopy(names);
	}

}
