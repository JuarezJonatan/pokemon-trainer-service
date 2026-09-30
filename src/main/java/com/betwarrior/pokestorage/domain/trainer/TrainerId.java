package com.betwarrior.pokestorage.domain.trainer;

import java.util.Objects;
import java.util.UUID;

public record TrainerId(UUID value) {

	public TrainerId {
		Objects.requireNonNull(value, "Trainer id is required");
	}

	public static TrainerId random() {
		return new TrainerId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
