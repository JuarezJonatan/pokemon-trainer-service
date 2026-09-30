package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemPocket(
		int id,
		String name,
		List<NamedRef<ItemCategory>> categories,
		List<Name> names) implements NamedResource, Localized {

	public ItemPocket {
		categories = Lists.nullSafeCopy(categories);
		names = Lists.nullSafeCopy(names);
	}

}
