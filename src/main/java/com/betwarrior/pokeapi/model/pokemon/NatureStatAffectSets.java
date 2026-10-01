package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.ref.NamedRef;

public record NatureStatAffectSets(
		List<NamedRef<Nature>> increase,
		List<NamedRef<Nature>> decrease) {

	public NatureStatAffectSets {
		increase = Lists.nullSafeCopy(increase);
		decrease = Lists.nullSafeCopy(decrease);
	}

}
