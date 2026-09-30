package com.betwarrior.pokestorage.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.StorageArea;
import com.betwarrior.pokestorage.domain.TrainerId;
import com.betwarrior.pokestorage.domain.TrainerStorage;

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
