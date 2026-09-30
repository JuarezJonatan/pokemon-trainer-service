package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.evolution.EvolutionChain;
import com.betwarrior.pokeapi.model.utility.GenerationGameIndex;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.MachineVersionDetail;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.model.utility.VerboseEffect;
import com.betwarrior.pokeapi.model.utility.VersionGroupFlavorText;
import com.betwarrior.pokeapi.ref.ApiRef;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Item(
		int id,
		String name,
		Integer flingPower,
		NamedRef<ItemFlingEffect> flingEffect,
		List<NamedRef<ItemAttribute>> attributes,
		NamedRef<ItemCategory> category,
		List<VerboseEffect> effectEntries,
		List<VersionGroupFlavorText> flavorTextEntries,
		List<GenerationGameIndex> gameIndices,
		List<Name> names,
		ItemSprites sprites,
		List<ItemHolderPokemon> heldByPokemon,
		ApiRef<EvolutionChain> babyTriggerFor,
		List<MachineVersionDetail> machines,
		List<ItemPrice> prices) implements NamedResource, Localized {

	public Item {
		attributes = Lists.nullSafeCopy(attributes);
		effectEntries = Lists.nullSafeCopy(effectEntries);
		flavorTextEntries = Lists.nullSafeCopy(flavorTextEntries);
		gameIndices = Lists.nullSafeCopy(gameIndices);
		names = Lists.nullSafeCopy(names);
		heldByPokemon = Lists.nullSafeCopy(heldByPokemon);
		machines = Lists.nullSafeCopy(machines);
		prices = Lists.nullSafeCopy(prices);
	}

}
