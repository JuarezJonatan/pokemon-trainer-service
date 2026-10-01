package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonStat(
		NamedRef<Stat> stat,
		int effort,
		int baseStat) {

}
