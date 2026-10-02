package com.betwarrior.pokestorage.domain.pokemon;

import java.util.Objects;
import java.util.Optional;

import com.betwarrior.pokestorage.domain.exception.EvolutionNotAllowedException;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.exception.PokemonNotInTeamException;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.species.SpeciesRef;
import com.betwarrior.pokestorage.domain.stats.EffortValues;
import com.betwarrior.pokestorage.domain.stats.IndividualValues;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

/**
 * An individual Pokemon owned by a trainer. Two specimens of the same species differ in
 * their genetics, training, nature, ability, origin and moves.
 * <p>
 * {@code version} counts the stored changes of the specimen. Changes made here keep it; the repository
 * rejects an update whose version is no longer the stored one, so concurrent changes are never lost.
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
		StorageSlot slot,
		long version) {

	public static final long FIRST_VERSION = 0;

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
		if (version < FIRST_VERSION) {
			throw new InvalidValueException("Version cannot be negative");
		}
	}

	public boolean isInTeam() {
		return slot.isIn(StorageArea.TEAM);
	}

	public PokemonSpecimen storedAt(StorageSlot newSlot) {
		return new PokemonSpecimen(id, owner, species, nickname, level, individualValues, effortValues, nature,
				ability, gender, shiny, origin, moves, heldItem, newSlot, version);
	}

	public PokemonSpecimen withVersion(long newVersion) {
		return new PokemonSpecimen(id, owner, species, nickname, level, individualValues, effortValues, nature,
				ability, gender, shiny, origin, moves, heldItem, slot, newVersion);
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
				newAbility, gender, shiny, origin, moves, heldItem, slot, version);
	}

}
