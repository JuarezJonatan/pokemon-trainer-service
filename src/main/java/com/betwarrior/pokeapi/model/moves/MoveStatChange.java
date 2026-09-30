package com.betwarrior.pokeapi.model.moves;

import com.betwarrior.pokeapi.model.pokemon.Stat;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveStatChange(
		Integer change,
		NamedRef<Stat> stat) {

}
