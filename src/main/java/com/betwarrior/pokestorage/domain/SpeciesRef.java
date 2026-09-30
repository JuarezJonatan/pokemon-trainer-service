package com.betwarrior.pokestorage.domain;

public record SpeciesRef(int id, String name) {

	public SpeciesRef {
		if (id <= 0 || name == null || name.isBlank()) {
			throw new InvalidValueException("A species needs a positive id and a name");
		}
	}

}
