package com.betwarrior.pokestorage.application.port;

import java.util.Map;

import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Stores trainers' Pokemon. Implementations must reject two Pokemon of the same trainer in the
 * same slot by failing with {@link com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException},
 * and an update whose {@link PokemonSpecimen#version()} is not the stored one by failing with
 * {@link com.betwarrior.pokestorage.application.exception.PokemonChangedConcurrentlyException}.
 */
public interface PokemonRepository {

	Mono<PokemonSpecimen> insert(PokemonSpecimen pokemon);

	/**
	 * Stores the changes of a Pokemon read at {@code pokemon.version()} and returns it with its next version.
	 */
	Mono<PokemonSpecimen> update(PokemonSpecimen pokemon);

	Mono<PokemonSpecimen> findByOwner(TrainerId owner, PokemonId id);

	Flux<PokemonSpecimen> findInArea(TrainerId owner, StorageArea area, int offset, int limit);

	Mono<Long> countInArea(TrainerId owner, StorageArea area);

	Mono<Map<PokemonId, StorageSlot>> occupiedSlots(TrainerId owner);

	/**
	 * Every trainer's Pokemon, in the order they were stored.
	 */
	Flux<PokemonSpecimen> findAll(int offset, int limit);

	Mono<Long> count();

}
