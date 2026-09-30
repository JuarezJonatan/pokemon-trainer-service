package com.betwarrior.pokeapi.model.evolution;

import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.model.locations.Location;
import com.betwarrior.pokeapi.model.locations.Region;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.model.pokemon.Type;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.ref.NamedRef;

public record EvolutionDetail(
		NamedRef<Item> item,
		NamedRef<EvolutionTrigger> trigger,
		Integer gender,
		NamedRef<Item> heldItem,
		NamedRef<Move> knownMove,
		NamedRef<Type> knownMoveType,
		NamedRef<Location> location,
		Integer minLevel,
		Integer minHappiness,
		Integer minBeauty,
		Integer minAffection,
		boolean needsOverworldRain,
		NamedRef<PokemonSpecies> partySpecies,
		NamedRef<Type> partyType,
		Integer relativePhysicalStats,
		String timeOfDay,
		NamedRef<PokemonSpecies> tradeSpecies,
		boolean turnUpsideDown,
		boolean isDefault,
		NamedRef<VersionGroup> versionGroup,
		boolean nearSpecialRock,
		boolean needsMultiplayer,
		NamedRef<Region> region,
		NamedRef<Move> usedMove,
		Integer minMoveCount,
		Integer minSteps,
		Integer minDamageTaken) {

}
