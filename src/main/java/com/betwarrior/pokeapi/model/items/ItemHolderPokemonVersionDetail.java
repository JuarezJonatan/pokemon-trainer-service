package com.betwarrior.pokeapi.model.items;

import com.betwarrior.pokeapi.model.games.Version;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemHolderPokemonVersionDetail(
		Integer rarity,
		NamedRef<Version> version) {

}
