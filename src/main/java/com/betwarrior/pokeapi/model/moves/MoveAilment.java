package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveAilment(
		int id,
		String name,
		List<NamedRef<Move>> moves,
		List<Name> names) implements NamedResource, Localized {

	public MoveAilment {
		moves = Lists.nullSafeCopy(moves);
		names = Lists.nullSafeCopy(names);
	}

}
