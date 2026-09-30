package com.betwarrior.pokestorage.domain.exception;

import com.betwarrior.pokestorage.domain.pokemon.PokemonId;

public class PokemonNotInTeamException extends DomainException {

	public PokemonNotInTeamException(PokemonId pokemon) {
		super("Pokemon %s must be in the active team to evolve".formatted(pokemon));
	}

}
