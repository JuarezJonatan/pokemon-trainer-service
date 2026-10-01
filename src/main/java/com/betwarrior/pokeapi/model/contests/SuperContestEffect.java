package com.betwarrior.pokeapi.model.contests;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.Resource;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.model.utility.FlavorText;
import com.betwarrior.pokeapi.ref.NamedRef;

public record SuperContestEffect(
		int id,
		Integer appeal,
		List<FlavorText> flavorTextEntries,
		List<NamedRef<Move>> moves) implements Resource {

	public SuperContestEffect {
		flavorTextEntries = Lists.nullSafeCopy(flavorTextEntries);
		moves = Lists.nullSafeCopy(moves);
	}

}
