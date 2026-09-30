package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Effect;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemFlingEffect(
		int id,
		String name,
		List<Effect> effectEntries,
		List<NamedRef<Item>> items) implements NamedResource {

	public ItemFlingEffect {
		effectEntries = Lists.nullSafeCopy(effectEntries);
		items = Lists.nullSafeCopy(items);
	}

}
