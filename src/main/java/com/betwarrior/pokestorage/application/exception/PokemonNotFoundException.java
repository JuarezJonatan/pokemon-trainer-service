package com.betwarrior.pokestorage.application.exception;

import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

public class PokemonNotFoundException extends ApplicationException {

	public PokemonNotFoundException(TrainerId trainer, PokemonId pokemon) {
		super("Trainer %s has no Pokemon %s".formatted(trainer, pokemon));
	}

}
