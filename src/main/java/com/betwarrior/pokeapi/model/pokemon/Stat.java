package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.model.moves.MoveDamageClass;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.ApiRef;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Stat(
		int id,
		String name,
		Integer gameIndex,
		boolean isBattleOnly,
		MoveStatAffectSets affectingMoves,
		NatureStatAffectSets affectingNatures,
		List<ApiRef<Characteristic>> characteristics,
		NamedRef<MoveDamageClass> moveDamageClass,
		List<Name> names,
		List<NamedRef<Item>> affectingItems) implements NamedResource, Localized {

	public Stat {
		characteristics = Lists.nullSafeCopy(characteristics);
		names = Lists.nullSafeCopy(names);
		affectingItems = Lists.nullSafeCopy(affectingItems);
	}

}
