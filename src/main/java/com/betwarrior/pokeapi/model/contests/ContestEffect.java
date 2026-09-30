package com.betwarrior.pokeapi.model.contests;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.Resource;
import com.betwarrior.pokeapi.model.utility.Effect;
import com.betwarrior.pokeapi.model.utility.FlavorText;

public record ContestEffect(
		int id,
		Integer appeal,
		Integer jam,
		List<Effect> effectEntries,
		List<FlavorText> flavorTextEntries) implements Resource {

	public ContestEffect {
		effectEntries = Lists.nullSafeCopy(effectEntries);
		flavorTextEntries = Lists.nullSafeCopy(flavorTextEntries);
	}

}
