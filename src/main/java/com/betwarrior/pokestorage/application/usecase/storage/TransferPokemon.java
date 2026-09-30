package com.betwarrior.pokestorage.application.usecase.storage;

import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.application.exception.PokemonNotFoundException;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.TrainerStorage;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Mono;

/**
 * Moves a Pokemon between the active team and the PC box. Moving it to the area it already is in
 * leaves it untouched.
 */
@Component
public class TransferPokemon {

	private final PokemonRepository pokemon;
	private final StorageProperties storage;
	private final TransactionalOperator transaction;

	public TransferPokemon(PokemonRepository pokemon, StorageProperties storage, TransactionalOperator transaction) {
		this.pokemon = pokemon;
		this.storage = storage;
		this.transaction = transaction;
	}

	public Mono<PokemonSpecimen> transfer(TrainerId trainer, PokemonId id, StorageArea destination) {
		Mono<PokemonSpecimen> move = pokemon.findByOwner(trainer, id)
				.switchIfEmpty(Mono.error(() -> new PokemonNotFoundException(trainer, id)))
				.zipWith(pokemon.occupiedSlots(trainer))
				.flatMap(found -> {
					PokemonSpecimen current = found.getT1();
					TrainerStorage trainerStorage = new TrainerStorage(storage.capacity(), found.getT2());
					var slot = trainerStorage.slotForTransfer(id, destination);
					if (slot.equals(current.slot())) {
						return Mono.just(current);
					}
					return pokemon.update(current.storedAt(slot));
				});
		return transaction.transactional(Mono.defer(() -> move))
				.retryWhen(SlotRetry.onConcurrentSlotAssignment());
	}

}
