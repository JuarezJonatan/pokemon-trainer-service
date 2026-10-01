package com.betwarrior.pokeapi.model.locations;

import com.betwarrior.pokeapi.model.games.Version;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EncounterVersionDetails(
		Integer rate,
		NamedRef<Version> version) {

}
