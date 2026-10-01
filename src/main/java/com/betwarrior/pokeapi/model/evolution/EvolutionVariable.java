package com.betwarrior.pokeapi.model.evolution;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EvolutionVariable(
		int id,
		String name,
		String symbol,
		String dataType,
		String source,
		NamedRef<VersionGroup> versionGroup,
		List<Name> names,
		List<Description> descriptions) implements NamedResource, Localized {

	public EvolutionVariable {
		names = Lists.nullSafeCopy(names);
		descriptions = Lists.nullSafeCopy(descriptions);
	}

}
