package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonHeldItem(
		NamedRef<Item> item,
		List<PokemonHeldItemVersion> versionDetails) {

	public PokemonHeldItem {
		versionDetails = Lists.nullSafeCopy(versionDetails);
	}

}
