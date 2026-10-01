package com.betwarrior.pokeapi.model.items;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ItemHolderPokemon(
		NamedRef<Pokemon> pokemon,
		List<ItemHolderPokemonVersionDetail> versionDetails) {

	public ItemHolderPokemon {
		versionDetails = Lists.nullSafeCopy(versionDetails);
	}

}
