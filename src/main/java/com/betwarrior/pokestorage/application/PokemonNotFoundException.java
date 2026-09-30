package com.betwarrior.pokestorage.application;

import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.TrainerId;

public class PokemonNotFoundException extends ApplicationException {

	public PokemonNotFoundException(TrainerId trainer, PokemonId pokemon) {
		super("Trainer %s has no Pokemon %s".formatted(trainer, pokemon));
	}

}
