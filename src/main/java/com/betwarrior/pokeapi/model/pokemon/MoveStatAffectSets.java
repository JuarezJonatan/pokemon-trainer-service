package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;

public record MoveStatAffectSets(
		List<MoveStatAffect> increase,
		List<MoveStatAffect> decrease) {

	public MoveStatAffectSets {
		increase = Lists.nullSafeCopy(increase);
		decrease = Lists.nullSafeCopy(decrease);
	}

}
