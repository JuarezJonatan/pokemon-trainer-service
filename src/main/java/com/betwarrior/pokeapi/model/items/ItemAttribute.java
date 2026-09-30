package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemAttribute(
		int id,
		String name,
		List<NamedRef<Item>> items,
		List<Name> names,
		List<Description> descriptions) implements NamedResource, Localized {

	public ItemAttribute {
		items = Lists.nullSafeCopy(items);
		names = Lists.nullSafeCopy(names);
		descriptions = Lists.nullSafeCopy(descriptions);
	}

}
