package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.model.moves.MoveDamageClass;
import com.betwarrior.pokeapi.model.utility.GenerationGameIndex;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Type(
		int id,
		String name,
		TypeRelations damageRelations,
		List<TypeRelationsPast> pastDamageRelations,
		List<GenerationGameIndex> gameIndices,
		NamedRef<Generation> generation,
		NamedRef<MoveDamageClass> moveDamageClass,
		List<Name> names,
		List<TypePokemon> pokemon,
		List<NamedRef<Move>> moves) implements NamedResource, Localized {

	public Type {
		pastDamageRelations = Lists.nullSafeCopy(pastDamageRelations);
		gameIndices = Lists.nullSafeCopy(gameIndices);
		names = Lists.nullSafeCopy(names);
		pokemon = Lists.nullSafeCopy(pokemon);
		moves = Lists.nullSafeCopy(moves);
	}

}
