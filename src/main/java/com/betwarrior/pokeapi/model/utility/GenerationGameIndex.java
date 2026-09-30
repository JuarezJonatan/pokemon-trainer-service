package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.ref.NamedRef;

public record GenerationGameIndex(
		Integer gameIndex,
		NamedRef<Generation> generation) {

}
