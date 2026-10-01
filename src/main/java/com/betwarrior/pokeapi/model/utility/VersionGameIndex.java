package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.model.games.Version;
import com.betwarrior.pokeapi.ref.NamedRef;

public record VersionGameIndex(
		Integer gameIndex,
		NamedRef<Version> version) {

}
