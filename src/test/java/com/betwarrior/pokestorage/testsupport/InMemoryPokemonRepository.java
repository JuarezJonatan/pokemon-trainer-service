package com.betwarrior.pokestorage.testsupport;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.betwarrior.pokestorage.application.exception.PokemonChangedConcurrentlyException;
import com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryPokemonRepository implements PokemonRepository {

	private final Map<PokemonId, PokemonSpecimen> pokemon = new ConcurrentHashMap<>();
	private final Map<PokemonId, Long> storageOrder = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong();
	private final AtomicInteger slotConflictsToSimulate = new AtomicInteger();
	private final AtomicReference<UnaryOperator<PokemonSpecimen>> concurrentChangeToSimulate = new AtomicReference<>();

	public void simulateConcurrentSlotConflicts(int times) {
		slotConflictsToSimulate.set(times);
	}

	/**
	 * Right before the next update, another operation changes the stored Pokemon (and its version).
	 */
	public void simulateConcurrentChange(UnaryOperator<PokemonSpecimen> change) {
		concurrentChangeToSimulate.set(change);
	}

	public PokemonSpecimen stored(PokemonId id) {
		return pokemon.get(id);
	}

	@Override
	public Mono<PokemonSpecimen> insert(PokemonSpecimen specimen) {
		return Mono.fromCallable(() -> {
			rejectTakenSlot(specimen);
			pokemon.put(specimen.id(), specimen);
			storageOrder.putIfAbsent(specimen.id(), sequence.incrementAndGet());
			return specimen;
		});
	}

	@Override
	public Mono<PokemonSpecimen> update(PokemonSpecimen specimen) {
		return Mono.fromCallable(() -> {
			UnaryOperator<PokemonSpecimen> concurrentChange = concurrentChangeToSimulate.getAndSet(null);
			if (concurrentChange != null) {
				PokemonSpecimen current = pokemon.get(specimen.id());
				pokemon.put(current.id(), concurrentChange.apply(current).withVersion(current.version() + 1));
			}
			if (pokemon.get(specimen.id()).version() != specimen.version()) {
				throw new PokemonChangedConcurrentlyException(specimen.id());
			}
			rejectTakenSlot(specimen);
			PokemonSpecimen updated = specimen.withVersion(specimen.version() + 1);
			pokemon.put(updated.id(), updated);
			return updated;
		});
	}

	@Override
	public Mono<PokemonSpecimen> findByOwner(TrainerId owner, PokemonId id) {
		return Mono.fromSupplier(() -> pokemon.get(id)).filter(found -> found.owner().equals(owner));
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

	@Override
	public Flux<PokemonSpecimen> findAll(int offset, int limit) {
		return Flux.fromStream(() -> pokemon.values().stream()
				.sorted(Comparator.comparingLong(specimen -> storageOrder.get(specimen.id())))
				.skip(offset)
				.limit(limit)
				.toList()
				.stream());
	}

	@Override
	public Mono<Long> count() {
		return Mono.fromSupplier(() -> (long) pokemon.size());
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
