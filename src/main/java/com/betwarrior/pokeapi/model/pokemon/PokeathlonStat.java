package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;

public record PokeathlonStat(
		int id,
		String name,
		List<Name> names,
		NaturePokeathlonStatAffectSets affectingNatures) implements NamedResource, Localized {

	public PokeathlonStat {
		names = Lists.nullSafeCopy(names);
	}

}
