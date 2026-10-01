package com.betwarrior.pokeapi.model.abilities;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.utility.Effect;
import com.betwarrior.pokeapi.ref.NamedRef;

public record AbilityEffectChange(
		List<Effect> effectEntries,
		NamedRef<VersionGroup> versionGroup) {

	public AbilityEffectChange {
		effectEntries = Lists.nullSafeCopy(effectEntries);
	}

}
