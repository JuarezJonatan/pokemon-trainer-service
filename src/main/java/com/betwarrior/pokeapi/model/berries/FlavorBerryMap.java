package com.betwarrior.pokeapi.model.berries;

import com.betwarrior.pokeapi.ref.NamedRef;

public record FlavorBerryMap(
		Integer potency,
		NamedRef<Berry> berry) {

}
