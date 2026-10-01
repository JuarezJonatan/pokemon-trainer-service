package com.betwarrior.pokeapi.model.locations;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.GenerationGameIndex;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Location(
		int id,
		String name,
		NamedRef<Region> region,
		List<Name> names,
		List<GenerationGameIndex> gameIndices,
		List<NamedRef<LocationArea>> areas) implements NamedResource, Localized {

	public Location {
		names = Lists.nullSafeCopy(names);
		gameIndices = Lists.nullSafeCopy(gameIndices);
		areas = Lists.nullSafeCopy(areas);
	}

}
