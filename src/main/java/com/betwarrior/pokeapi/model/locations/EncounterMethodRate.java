package com.betwarrior.pokeapi.model.locations;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.encounters.EncounterMethod;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EncounterMethodRate(
		NamedRef<EncounterMethod> encounterMethod,
		List<EncounterVersionDetails> versionDetails) {

	public EncounterMethodRate {
		versionDetails = Lists.nullSafeCopy(versionDetails);
	}

}
