package com.betwarrior.pokestorage.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Snapshot of which slots a trainer currently occupies. Decides where a Pokemon goes;
 * slots are never compacted so that a Pokemon keeps its exact team position.
 */
public final class TrainerStorage {

	private final StorageCapacity capacity;
	private final Map<PokemonId, StorageSlot> occupied;

	public TrainerStorage(StorageCapacity capacity, Map<PokemonId, StorageSlot> occupied) {
		this.capacity = Objects.requireNonNull(capacity);
		this.occupied = Map.copyOf(occupied);
	}

	public StorageSlot slotForNewCapture() {
		return firstFreeSlotIn(StorageArea.TEAM)
				.or(() -> firstFreeSlotIn(StorageArea.BOX))
				.orElseThrow(StorageFullException::teamAndBox);
	}

	public StorageSlot slotForTransfer(PokemonId pokemon, StorageArea destination) {
		StorageSlot current = occupied.get(pokemon);
		if (current == null) {
			throw new IllegalArgumentException("Pokemon " + pokemon + " is not stored by this trainer");
		}
		if (current.isIn(destination)) {
			return current;
		}
		return firstFreeSlotIn(destination)
				.orElseThrow(() -> StorageFullException.of(destination, capacity.of(destination)));
	}

	private Optional<StorageSlot> firstFreeSlotIn(StorageArea area) {
		Set<Integer> taken = occupied.values().stream()
				.filter(slot -> slot.isIn(area))
				.map(StorageSlot::position)
				.collect(Collectors.toSet());
		return IntStream.rangeClosed(1, capacity.of(area))
				.filter(position -> !taken.contains(position))
				.mapToObj(position -> new StorageSlot(area, position))
				.findFirst();
	}

}
