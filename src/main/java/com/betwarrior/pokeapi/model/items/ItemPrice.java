package com.betwarrior.pokeapi.model.items;

import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemPrice(
		Integer purchasePrice,
		Integer sellPrice,
		NamedRef<Currency> currency,
		NamedRef<VersionGroup> versionGroup) {

}
