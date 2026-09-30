package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveTarget(
		int id,
		String name,
		List<Description> descriptions,
		List<NamedRef<Move>> moves,
		List<Name> names) implements NamedResource, Localized {

	public MoveTarget {
		descriptions = Lists.nullSafeCopy(descriptions);
		moves = Lists.nullSafeCopy(moves);
		names = Lists.nullSafeCopy(names);
	}

}
