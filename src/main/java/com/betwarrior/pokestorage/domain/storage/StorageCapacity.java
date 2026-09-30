package com.betwarrior.pokestorage.domain.storage;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

public record StorageCapacity(int team, int box) {

	public StorageCapacity {
		if (team < 1 || box < 1) {
			throw new InvalidValueException("Team and box capacities must be positive");
		}
	}

	public int of(StorageArea area) {
		return area == StorageArea.TEAM ? team : box;
	}

}
