package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.model.games.Version;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonHeldItemVersion(
		NamedRef<Version> version,
		Integer rarity) {

}
