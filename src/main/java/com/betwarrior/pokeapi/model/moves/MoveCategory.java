package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveCategory(
		int id,
		String name,
		List<NamedRef<Move>> moves,
		List<Description> descriptions) implements NamedResource {

	public MoveCategory {
		moves = Lists.nullSafeCopy(moves);
		descriptions = Lists.nullSafeCopy(descriptions);
	}

}
