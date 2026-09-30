package com.betwarrior.pokeapi.model.utility;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.games.Version;
import com.betwarrior.pokeapi.ref.NamedRef;

public record VersionEncounterDetail(
		NamedRef<Version> version,
		Integer maxChance,
		List<Encounter> encounterDetails) {

	public VersionEncounterDetail {
		encounterDetails = Lists.nullSafeCopy(encounterDetails);
	}

}
