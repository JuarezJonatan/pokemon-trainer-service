package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.pokemon.Type;
import com.betwarrior.pokeapi.model.utility.VerboseEffect;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PastMoveStatValues(
		Integer accuracy,
		Integer effectChance,
		Integer power,
		Integer pp,
		List<VerboseEffect> effectEntries,
		NamedRef<Type> type,
		NamedRef<VersionGroup> versionGroup) {

	public PastMoveStatValues {
		effectEntries = Lists.nullSafeCopy(effectEntries);
	}

}
