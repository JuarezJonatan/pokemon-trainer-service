package com.betwarrior.pokeapi.model.games;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.locations.Region;
import com.betwarrior.pokeapi.model.moves.MoveLearnMethod;
import com.betwarrior.pokeapi.ref.NamedRef;

public record VersionGroup(
		int id,
		String name,
		int order,
		NamedRef<Generation> generation,
		List<NamedRef<MoveLearnMethod>> moveLearnMethods,
		List<NamedRef<Pokedex>> pokedexes,
		List<NamedRef<Region>> regions,
		List<NamedRef<Version>> versions) implements NamedResource {

	public VersionGroup {
		moveLearnMethods = Lists.nullSafeCopy(moveLearnMethods);
		pokedexes = Lists.nullSafeCopy(pokedexes);
		regions = Lists.nullSafeCopy(regions);
		versions = Lists.nullSafeCopy(versions);
	}

}
