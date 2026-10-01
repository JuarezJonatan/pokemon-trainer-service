package com.betwarrior.pokeapi.model.utility;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.encounters.EncounterConditionValue;
import com.betwarrior.pokeapi.model.encounters.EncounterMethod;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Encounter(
		Integer minLevel,
		Integer maxLevel,
		List<NamedRef<EncounterConditionValue>> conditionValues,
		Integer chance,
		NamedRef<EncounterMethod> method) {

	public Encounter {
		conditionValues = Lists.nullSafeCopy(conditionValues);
	}

}
