package com.betwarrior.pokestorage.testsupport;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.betwarrior.pokestorage.application.PokemonRepository;
import com.betwarrior.pokestorage.application.SlotAlreadyTakenException;
import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.StorageArea;
import com.betwarrior.pokestorage.domain.StorageSlot;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryPokemonRepository implements PokemonRepository {

	private final Map<PokemonId, PokemonSpecimen> pokemon = new ConcurrentHashMap<>();
	private final AtomicInteger slotConflictsToSimulate = new AtomicInteger();

	public void simulateConcurrentSlotConflicts(int times) {
		slotConflictsToSimulate.set(times);
	}

	public PokemonSpecimen stored(PokemonId id) {
		return pokemon.get(id);
	}

	@Override
	public Mono<PokemonSpecimen> insert(PokemonSpecimen specimen) {
		return Mono.fromCallable(() -> {
			rejectTakenSlot(specimen);
			pokemon.put(specimen.id(), specimen);
			return specimen;
		});
	}

	@Override
	public Mono<PokemonSpecimen> update(PokemonSpecimen specimen) {
		return Mono.fromCallable(() -> {
			rejectTakenSlot(specimen);
			pokemon.put(specimen.id(), specimen);
			return specimen;
		});
	}

	@Override
	public Mono<PokemonSpecimen> findByOwner(TrainerId owner, PokemonId id) {
		return Mono.justOrEmpty(pokemon.get(id)).filter(found -> found.owner().equals(owner));
	}

	@Override
	public Flux<PokemonSpecimen> findInArea(TrainerId owner, StorageArea area, int offset, int limit) {
		return Flux.fromStream(() -> ownedBy(owner)
				.filter(specimen -> specimen.slot().isIn(area))
				.sorted(Comparator.comparingInt(specimen -> specimen.slot().position()))
				.skip(offset)
				.limit(limit)
				.toList()
				.stream());
	}

	@Override
	public Mono<Long> countInArea(TrainerId owner, StorageArea area) {
		return Mono.fromSupplier(() -> ownedBy(owner).filter(specimen -> specimen.slot().isIn(area)).count());
	}

	@Override
	public Mono<Map<PokemonId, StorageSlot>> occupiedSlots(TrainerId owner) {
		return Mono.fromSupplier(() -> ownedBy(owner)
				.collect(Collectors.toMap(PokemonSpecimen::id, PokemonSpecimen::slot)));
	}

	private Stream<PokemonSpecimen> ownedBy(TrainerId owner) {
		return pokemon.values().stream().filter(specimen -> specimen.owner().equals(owner));
	}

	private void rejectTakenSlot(PokemonSpecimen specimen) {
		if (slotConflictsToSimulate.getAndUpdate(remaining -> Math.max(0, remaining - 1)) > 0) {
			throw new SlotAlreadyTakenException(null);
		}
		boolean taken = ownedBy(specimen.owner())
				.anyMatch(other -> !other.id().equals(specimen.id()) && other.slot().equals(specimen.slot()));
		if (taken) {
			throw new SlotAlreadyTakenException(null);
		}
	}

}
