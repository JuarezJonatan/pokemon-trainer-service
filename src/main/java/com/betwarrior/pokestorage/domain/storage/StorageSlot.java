package com.betwarrior.pokestorage.domain.storage;

import java.util.Objects;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

public record StorageSlot(StorageArea area, int position) {

	public StorageSlot {
		Objects.requireNonNull(area, "Storage area is required");
		if (position < 1) {
			throw new InvalidValueException("Slot position must be positive but was " + position);
		}
	}

	public static StorageSlot team(int position) {
		return new StorageSlot(StorageArea.TEAM, position);
	}

	public static StorageSlot box(int position) {
		return new StorageSlot(StorageArea.BOX, position);
	}

	public boolean isIn(StorageArea other) {
		return area == other;
	}

}
