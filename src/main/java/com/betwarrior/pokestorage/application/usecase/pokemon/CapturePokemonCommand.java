package com.betwarrior.pokestorage.application.usecase.pokemon;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.betwarrior.pokestorage.domain.pokemon.Gender;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.stats.StatValues;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

public record CapturePokemonCommand(
		TrainerId trainer,
		String species,
		Optional<String> nickname,
		int level,
		StatValues individualValues,
		Optional<StatValues> effortValues,
		Nature nature,
		String ability,
		Gender gender,
		boolean shiny,
		Optional<String> originalTrainerId,
		String pokeball,
		Optional<Instant> caughtAt,
		String metLocation,
		List<String> moves,
		Optional<String> heldItem) {
}
