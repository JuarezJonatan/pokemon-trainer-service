package com.betwarrior.pokeapi.model.pokemon;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.model.utility.VersionGameIndex;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Pokemon(
		int id,
		String name,
		Integer baseExperience,
		int height,
		boolean isDefault,
		int order,
		int weight,
		List<PokemonAbility> abilities,
		List<NamedRef<PokemonForm>> forms,
		List<VersionGameIndex> gameIndices,
		List<PokemonHeldItem> heldItems,
		String locationAreaEncounters,
		List<PokemonMove> moves,
		PokemonSprites sprites,
		NamedRef<PokemonSpecies> species,
		List<PokemonStat> stats,
		List<PokemonType> types,
		List<PokemonTypePast> pastTypes,
		PokemonCries cries,
		List<PokemonAbilityPast> pastAbilities,
		List<PokemonStatPast> pastStats) implements NamedResource {

	public Pokemon {
		abilities = Lists.nullSafeCopy(abilities);
		forms = Lists.nullSafeCopy(forms);
		gameIndices = Lists.nullSafeCopy(gameIndices);
		heldItems = Lists.nullSafeCopy(heldItems);
		moves = Lists.nullSafeCopy(moves);
		stats = Lists.nullSafeCopy(stats);
		types = Lists.nullSafeCopy(types);
		pastTypes = Lists.nullSafeCopy(pastTypes);
		pastAbilities = Lists.nullSafeCopy(pastAbilities);
		pastStats = Lists.nullSafeCopy(pastStats);
	}

	/**
	 * Base stats keyed by stat name ({@code hp}, {@code attack}, ...), in PokéAPI order.
	 */
	public Map<String, Integer> baseStats() {
		Map<String, Integer> baseStats = new LinkedHashMap<>();
		stats.forEach(stat -> baseStats.put(stat.stat().name(), stat.baseStat()));
		return Collections.unmodifiableMap(baseStats);
	}

	public Set<String> moveNames() {
		return moves.stream()
				.map(move -> move.move().name())
				.collect(Collectors.toUnmodifiableSet());
	}

	public boolean learns(String move) {
		return moves.stream().anyMatch(learnable -> learnable.move().name().equals(move));
	}

	public List<String> typeNames() {
		return types.stream()
				.sorted(Comparator.comparingInt(PokemonType::slot))
				.map(type -> type.type().name())
				.toList();
	}

	public Optional<String> frontSprite() {
		return Optional.ofNullable(sprites).map(PokemonSprites::frontDefault);
	}

}
