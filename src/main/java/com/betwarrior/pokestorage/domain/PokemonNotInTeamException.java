package com.betwarrior.pokestorage.domain;

public class PokemonNotInTeamException extends DomainException {

	public PokemonNotInTeamException(PokemonId pokemon) {
		super("Pokemon %s must be in the active team to evolve".formatted(pokemon));
	}

}
