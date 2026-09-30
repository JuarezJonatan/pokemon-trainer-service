package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemCategory(
		int id,
		String name,
		List<NamedRef<Item>> items,
		List<Name> names,
		NamedRef<ItemPocket> pocket) implements NamedResource, Localized {

	public ItemCategory {
		items = Lists.nullSafeCopy(items);
		names = Lists.nullSafeCopy(names);
	}

}
