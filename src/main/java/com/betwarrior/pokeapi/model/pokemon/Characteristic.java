package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.Resource;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Characteristic(
		int id,
		Integer geneModulo,
		List<Integer> possibleValues,
		NamedRef<Stat> highestStat,
		List<Description> descriptions) implements Resource {

	public Characteristic {
		possibleValues = Lists.nullSafeCopy(possibleValues);
		descriptions = Lists.nullSafeCopy(descriptions);
	}

}
