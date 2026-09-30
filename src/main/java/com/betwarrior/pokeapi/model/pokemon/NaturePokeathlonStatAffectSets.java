package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;

public record NaturePokeathlonStatAffectSets(
		List<NaturePokeathlonStatAffect> increase,
		List<NaturePokeathlonStatAffect> decrease) {

	public NaturePokeathlonStatAffectSets {
		increase = Lists.nullSafeCopy(increase);
		decrease = Lists.nullSafeCopy(decrease);
	}

}
