package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.ref.NamedRef;

public record TypeRelationsPast(
		NamedRef<Generation> generation,
		TypeRelations damageRelations) {

}
