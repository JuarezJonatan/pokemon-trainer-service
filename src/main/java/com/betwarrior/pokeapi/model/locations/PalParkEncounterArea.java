package com.betwarrior.pokeapi.model.locations;

import com.betwarrior.pokeapi.ref.NamedRef;

public record PalParkEncounterArea(
		Integer baseScore,
		Integer rate,
		NamedRef<PalParkArea> area) {

}
