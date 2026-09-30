package com.betwarrior.pokestorage.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * An individual Pokemon owned by a trainer. Two specimens of the same species differ in
 * their genetics, training, nature, ability, origin and moves.
 */
public record PokemonSpecimen(
		PokemonId id,
		TrainerId owner,
		SpeciesRef species,
		Optional<String> nickname,
		Level level,
		IndividualValues individualValues,
		EffortValues effortValues,
		Nature nature,
		String ability,
		Gender gender,
		boolean shiny,
		CaptureOrigin origin,
		MoveSet moves,
		Optional<String> heldItem,
		StorageSlot slot) {

	public PokemonSpecimen {
		Objects.requireNonNull(id);
		Objects.requireNonNull(owner);
		Objects.requireNonNull(species);
		Objects.requireNonNull(nickname);
		Objects.requireNonNull(level);
		Objects.requireNonNull(individualValues);
		Objects.requireNonNull(effortValues);
		Objects.requireNonNull(nature, "Nature is required");
		Objects.requireNonNull(gender, "Gender is required");
		Objects.requireNonNull(origin);
		Objects.requireNonNull(moves);
		Objects.requireNonNull(heldItem);
		Objects.requireNonNull(slot);
		if (ability == null || ability.isBlank()) {
			throw new InvalidValueException("Ability is required");
		}
	}

	public boolean isInTeam() {
		return slot.isIn(StorageArea.TEAM);
	}

	public PokemonSpecimen storedAt(StorageSlot newSlot) {
		return new PokemonSpecimen(id, owner, species, nickname, level, individualValues, effortValues, nature,
				ability, gender, shiny, origin, moves, heldItem, newSlot);
	}

	public PokemonSpecimen evolveInto(Species current, Species target, Optional<String> requestedAbility) {
		if (!current.ref().equals(species)) {
			throw new IllegalArgumentException("Current species data does not match " + species.name());
		}
		if (!isInTeam()) {
			throw new PokemonNotInTeamException(id);
		}
		if (!target.evolvesDirectlyFrom(species)) {
			throw new EvolutionNotAllowedException(
					"%s is not a direct evolution of %s".formatted(target.name(), species.name()));
		}
		String newAbility = target.abilityAfterEvolvingFrom(current, ability, requestedAbility);
		return new PokemonSpecimen(id, owner, target.ref(), nickname, level, individualValues, effortValues, nature,
				newAbility, gender, shiny, origin, moves, heldItem, slot);
	}

}
