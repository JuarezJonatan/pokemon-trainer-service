package com.betwarrior.pokeapi.model.games;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Version(
		int id,
		String name,
		List<Name> names,
		NamedRef<VersionGroup> versionGroup) implements NamedResource, Localized {

	public Version {
		names = Lists.nullSafeCopy(names);
	}

}
