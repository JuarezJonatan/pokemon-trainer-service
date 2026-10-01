package com.betwarrior.pokestorage.application.exception;

import com.betwarrior.pokestorage.domain.pokemon.PokemonId;

/**
 * The Pokemon was updated by another operation after it was read (optimistic lock).
 */
public class PokemonChangedConcurrentlyException extends ConcurrentUpdateException {

	public PokemonChangedConcurrentlyException(PokemonId id) {
		super("Pokemon %s was changed by a concurrent operation".formatted(id.value()), null);
	}

}
