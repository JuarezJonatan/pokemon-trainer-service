package com.betwarrior.pokestorage.domain;

import java.util.Objects;

public record Trainer(TrainerId id, String name) {

	public Trainer {
		Objects.requireNonNull(id, "Trainer id is required");
		if (name == null || name.isBlank()) {
			throw new InvalidValueException("Trainer name is required");
		}
	}

	public static Trainer register(String name) {
		return new Trainer(TrainerId.random(), name);
	}

}
