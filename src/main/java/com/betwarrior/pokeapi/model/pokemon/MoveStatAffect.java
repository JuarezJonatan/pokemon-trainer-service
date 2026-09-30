package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveStatAffect(
		Integer change,
		NamedRef<Move> move) {

}
