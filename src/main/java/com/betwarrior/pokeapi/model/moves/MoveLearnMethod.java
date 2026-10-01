package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveLearnMethod(
		int id,
		String name,
		List<Description> descriptions,
		List<Name> names,
		List<NamedRef<VersionGroup>> versionGroups) implements NamedResource, Localized {

	public MoveLearnMethod {
		descriptions = Lists.nullSafeCopy(descriptions);
		names = Lists.nullSafeCopy(names);
		versionGroups = Lists.nullSafeCopy(versionGroups);
	}

}
