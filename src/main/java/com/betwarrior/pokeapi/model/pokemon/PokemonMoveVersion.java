package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.moves.MoveLearnMethod;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonMoveVersion(
		NamedRef<MoveLearnMethod> moveLearnMethod,
		NamedRef<VersionGroup> versionGroup,
		Integer levelLearnedAt,
		Integer order) {

}
