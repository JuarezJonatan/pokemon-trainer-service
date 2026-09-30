package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonMove(
		NamedRef<Move> move,
		List<PokemonMoveVersion> versionGroupDetails) {

	public PokemonMove {
		versionGroupDetails = Lists.nullSafeCopy(versionGroupDetails);
	}

}
