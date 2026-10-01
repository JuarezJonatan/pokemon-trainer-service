package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.ref.NamedRef;

public record NatureStatChange(
		Integer maxChange,
		NamedRef<PokeathlonStat> pokeathlonStat) {

}
