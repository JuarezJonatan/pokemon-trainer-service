package com.betwarrior.pokeapi.model.encounters;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EncounterConditionValue(
		int id,
		String name,
		NamedRef<EncounterCondition> condition,
		List<Name> names) implements NamedResource, Localized {

	public EncounterConditionValue {
		names = Lists.nullSafeCopy(names);
	}

}
