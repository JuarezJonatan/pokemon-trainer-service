package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ContestComboDetail(
		List<NamedRef<Move>> useBefore,
		List<NamedRef<Move>> useAfter) {

	public ContestComboDetail {
		useBefore = Lists.nullSafeCopy(useBefore);
		useAfter = Lists.nullSafeCopy(useAfter);
	}

}
