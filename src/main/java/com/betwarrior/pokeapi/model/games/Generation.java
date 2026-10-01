package com.betwarrior.pokeapi.model.games;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.abilities.Ability;
import com.betwarrior.pokeapi.model.locations.Region;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.model.pokemon.Type;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Generation(
		int id,
		String name,
		List<NamedRef<Ability>> abilities,
		List<Name> names,
		List<NamedRef<Move>> moves,
		List<NamedRef<PokemonSpecies>> pokemonSpecies,
		List<NamedRef<Type>> types,
		List<NamedRef<VersionGroup>> versionGroups,
		NamedRef<Region> mainRegion) implements NamedResource, Localized {

	public Generation {
		abilities = Lists.nullSafeCopy(abilities);
		names = Lists.nullSafeCopy(names);
		moves = Lists.nullSafeCopy(moves);
		pokemonSpecies = Lists.nullSafeCopy(pokemonSpecies);
		types = Lists.nullSafeCopy(types);
		versionGroups = Lists.nullSafeCopy(versionGroups);
	}

}
