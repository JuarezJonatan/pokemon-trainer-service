package com.betwarrior.pokeapi.model.berries;

import com.betwarrior.pokeapi.ref.NamedRef;

public record BerryFlavorMap(
		Integer potency,
		NamedRef<BerryFlavor> flavor) {

}
