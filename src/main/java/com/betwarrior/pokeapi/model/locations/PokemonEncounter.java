package com.betwarrior.pokeapi.model.locations;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.model.utility.VersionEncounterDetail;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonEncounter(
		NamedRef<Pokemon> pokemon,
		List<VersionEncounterDetail> versionDetails) {

	public PokemonEncounter {
		versionDetails = Lists.nullSafeCopy(versionDetails);
	}

}
