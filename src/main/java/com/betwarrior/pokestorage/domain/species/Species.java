package com.betwarrior.pokestorage.domain.species;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.betwarrior.pokestorage.domain.exception.EvolutionNotAllowedException;
import com.betwarrior.pokestorage.domain.exception.SpeciesRuleViolationException;
import com.betwarrior.pokestorage.domain.pokemon.Gender;
import com.betwarrior.pokestorage.domain.pokemon.Level;
import com.betwarrior.pokestorage.domain.pokemon.MoveSet;
import com.betwarrior.pokestorage.domain.stats.EffortValues;
import com.betwarrior.pokestorage.domain.stats.IndividualValues;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.stats.StatCalculator;
import com.betwarrior.pokestorage.domain.stats.StatValues;

/**
 * Static data of a species as published by PokeAPI (default variety), plus the rules
 * that data imposes on individual Pokemon.
 */
public record Species(
		SpeciesRef ref,
		List<SpeciesAbility> abilities,
		Set<String> learnableMoves,
		GenderRatio genderRatio,
		StatValues baseStats,
		List<String> types,
		String spriteUrl,
		Optional<String> evolvesFrom) {

	public Species {
		Objects.requireNonNull(ref);
		abilities = List.copyOf(abilities);
		learnableMoves = Set.copyOf(learnableMoves);
		types = List.copyOf(types);
		Objects.requireNonNull(genderRatio);
		Objects.requireNonNull(baseStats);
		Objects.requireNonNull(evolvesFrom);
	}

	public String name() {
		return ref.name();
	}

	public void validateIndividual(String ability, MoveSet moves, Gender gender) {
		List<String> violations = new ArrayList<>();
		if (!hasAbility(ability)) {
			violations.add("%s cannot have ability '%s' (allowed: %s)".formatted(name(), ability, abilityNames()));
		}
		moves.moves().stream()
				.filter(move -> !learnableMoves.contains(move))
				.forEach(move -> violations.add("%s cannot learn move '%s'".formatted(name(), move)));
		if (!genderRatio.allows(gender)) {
			violations.add("%s cannot be %s".formatted(name(), gender));
		}
		if (!violations.isEmpty()) {
			throw new SpeciesRuleViolationException(violations);
		}
	}

	public boolean hasAbility(String ability) {
		return abilities.stream().anyMatch(candidate -> candidate.name().equals(ability));
	}

	public boolean evolvesDirectlyFrom(SpeciesRef previous) {
		return evolvesFrom.map(previous.name()::equals).orElse(false);
	}

	/**
	 * Ability a Pokemon of {@code previous} species gets when evolving into this one: the requested one
	 * if valid, otherwise the ability sharing the slot it had, otherwise the first non-hidden ability.
	 */
	public String abilityAfterEvolvingFrom(Species previous, String currentAbility, Optional<String> requested) {
		if (requested.isPresent()) {
			String ability = requested.get();
			if (!hasAbility(ability)) {
				throw new SpeciesRuleViolationException(List.of(
						"%s cannot have ability '%s' (allowed: %s)".formatted(name(), ability, abilityNames())));
			}
			return ability;
		}
		Optional<Integer> previousSlot = previous.abilities.stream()
				.filter(candidate -> candidate.name().equals(currentAbility))
				.map(SpeciesAbility::slot)
				.findFirst();
		return previousSlot
				.flatMap(slot -> abilities.stream().filter(candidate -> candidate.slot() == slot).findFirst())
				.or(() -> abilities.stream().filter(candidate -> !candidate.hidden())
						.min(Comparator.comparingInt(SpeciesAbility::slot)))
				.map(SpeciesAbility::name)
				.orElseThrow(() -> new EvolutionNotAllowedException(name() + " has no abilities to assign"));
	}

	public StatValues statsAt(Level level, IndividualValues ivs, EffortValues evs, Nature nature) {
		return StatCalculator.calculate(baseStats, level, ivs, evs, nature);
	}

	private List<String> abilityNames() {
		return abilities.stream().map(SpeciesAbility::name).toList();
	}

}
