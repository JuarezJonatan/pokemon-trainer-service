package com.betwarrior.pokestorage.domain.pokemon;

import java.time.Instant;
import java.util.Objects;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

public record CaptureOrigin(String originalTrainerId, String pokeball, Instant caughtAt, Level metLevel, String metLocation) {

	public CaptureOrigin {
		requireText(originalTrainerId, "Original trainer id");
		requireText(pokeball, "Pokeball");
		requireText(metLocation, "Met location");
		Objects.requireNonNull(caughtAt, "Capture date is required");
		Objects.requireNonNull(metLevel, "Met level is required");
	}

	private static void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException(field + " is required");
		}
	}

}
