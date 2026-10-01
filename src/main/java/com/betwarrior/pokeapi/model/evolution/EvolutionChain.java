package com.betwarrior.pokeapi.model.evolution;

import com.betwarrior.pokeapi.model.Resource;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EvolutionChain(
		int id,
		NamedRef<Item> babyTriggerItem,
		ChainLink chain) implements Resource {

}
