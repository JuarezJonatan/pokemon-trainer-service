package com.betwarrior.pokeapi.model.berries;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.model.pokemon.Type;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Berry(
		int id,
		String name,
		Integer growthTime,
		Integer maxHarvest,
		Integer naturalGiftPower,
		Integer size,
		Integer smoothness,
		Integer soilDryness,
		NamedRef<BerryFirmness> firmness,
		List<BerryFlavorMap> flavors,
		NamedRef<Item> item,
		NamedRef<Type> naturalGiftType) implements NamedResource {

	public Berry {
		flavors = Lists.nullSafeCopy(flavors);
	}

}
