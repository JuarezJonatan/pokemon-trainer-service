package com.betwarrior.pokestorage.application.usecase.pokemon;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.application.exception.InvalidItemException;
import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.application.usecase.storage.ConcurrentUpdateRetry;
import com.betwarrior.pokestorage.domain.pokemon.CaptureOrigin;
import com.betwarrior.pokestorage.domain.pokemon.Level;
import com.betwarrior.pokestorage.domain.pokemon.MoveSet;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.stats.EffortValues;
import com.betwarrior.pokestorage.domain.stats.IndividualValues;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.storage.TrainerStorage;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Registers a newly caught Pokemon: validates it against PokeAPI and stores it in the first free
 * team slot, or in the PC box when the team is full.
 */
@Component
@RequiredArgsConstructor
public class CapturePokemon {

	private final TrainerRepository trainers;
	private final PokemonRepository pokemon;
	private final PokemonCatalog catalog;
	private final StorageProperties storage;
	private final TransactionalOperator transaction;
	private final Clock clock;

	public Mono<PokemonSpecimen> capture(CapturePokemonCommand command) {
		return Mono.fromSupplier(() -> new Individual(command, clock))
				.flatMap(individual -> trainers.findById(command.trainer())
						.switchIfEmpty(Mono.error(() -> new TrainerNotFoundException(command.trainer())))
						.then(validateAgainstCatalog(command, individual))
						.flatMap(species -> store(command, individual, species)));
	}

	private Mono<Species> validateAgainstCatalog(CapturePokemonCommand command, Individual individual) {
		Mono<Species> species = catalog.findSpecies(CatalogNames.normalize(command.species()))
				.doOnNext(found -> found.validateIndividual(individual.ability, individual.moves, command.gender()));
		Mono<Boolean> pokeball = catalog.findItem(individual.origin.pokeball())
				.filter(PokemonCatalog.Item::isPokeball)
				.switchIfEmpty(Mono.error(() -> new InvalidItemException(
						"'%s' is not a pokeball".formatted(individual.origin.pokeball()))))
				.thenReturn(true);
		Mono<Boolean> heldItem = individual.heldItem
				.map(item -> catalog.findItem(item).thenReturn(true))
				.orElse(Mono.just(true));
		return Mono.zip(species, pokeball, heldItem).map(results -> results.getT1());
	}

	private Mono<PokemonSpecimen> store(CapturePokemonCommand command, Individual individual, Species species) {
		Mono<PokemonSpecimen> insert = pokemon.occupiedSlots(command.trainer())
				.map(occupied -> new TrainerStorage(storage.capacity(), occupied).slotForNewCapture())
				.map(slot -> individual.toSpecimen(command, species, slot))
				.flatMap(pokemon::insert);
		return transaction.transactional(Mono.defer(() -> insert))
				.retryWhen(ConcurrentUpdateRetry.boundedBy(storage.capacity()));
	}

	private static final class Individual {

		private final Level level;
		private final IndividualValues ivs;
		private final EffortValues evs;
		private final MoveSet moves;
		private final String ability;
		private final CaptureOrigin origin;
		private final Optional<String> heldItem;

		private Individual(CapturePokemonCommand command, Clock clock) {
			this.level = new Level(command.level());
			this.ivs = new IndividualValues(command.individualValues());
			this.evs = command.effortValues().map(EffortValues::new).orElseGet(EffortValues::none);
			this.moves = new MoveSet(command.moves().stream().map(CatalogNames::normalize).toList());
			this.ability = CatalogNames.normalize(command.ability());
			this.origin = new CaptureOrigin(
					command.originalTrainerId().orElse(command.trainer().toString()),
					CatalogNames.normalize(command.pokeball()),
					command.caughtAt().orElseGet(clock::instant),
					level,
					command.metLocation());
			this.heldItem = command.heldItem().map(CatalogNames::normalize);
		}

		private PokemonSpecimen toSpecimen(CapturePokemonCommand command, Species species, StorageSlot slot) {
			return new PokemonSpecimen(PokemonId.random(), command.trainer(), species.ref(), command.nickname(),
					level, ivs, evs, command.nature(), ability, command.gender(), command.shiny(), origin, moves,
					heldItem, slot, PokemonSpecimen.FIRST_VERSION);
		}

	}

}
