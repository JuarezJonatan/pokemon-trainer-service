package com.betwarrior.pokeapi.model.encounters;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EncounterCondition(
		int id,
		String name,
		List<Name> names,
		List<NamedRef<EncounterConditionValue>> values) implements NamedResource, Localized {

	public EncounterCondition {
		names = Lists.nullSafeCopy(names);
		values = Lists.nullSafeCopy(values);
	}

}
