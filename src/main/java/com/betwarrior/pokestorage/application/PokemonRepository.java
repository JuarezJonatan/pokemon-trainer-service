package com.betwarrior.pokestorage.application;

import java.util.Map;

import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.StorageArea;
import com.betwarrior.pokestorage.domain.StorageSlot;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Stores trainers' Pokemon. Implementations must reject two Pokemon of the same trainer in the
 * same slot by failing with {@link SlotAlreadyTakenException}.
 */
public interface PokemonRepository {

	Mono<PokemonSpecimen> insert(PokemonSpecimen pokemon);

	Mono<PokemonSpecimen> update(PokemonSpecimen pokemon);

	Mono<PokemonSpecimen> findByOwner(TrainerId owner, PokemonId id);

	Flux<PokemonSpecimen> findInArea(TrainerId owner, StorageArea area, int offset, int limit);

	Mono<Long> countInArea(TrainerId owner, StorageArea area);

	Mono<Map<PokemonId, StorageSlot>> occupiedSlots(TrainerId owner);

}
