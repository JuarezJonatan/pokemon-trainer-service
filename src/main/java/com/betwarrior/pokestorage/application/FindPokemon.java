package com.betwarrior.pokestorage.application;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Mono;

@Component
public class FindPokemon {

	private final PokemonRepository pokemon;

	public FindPokemon(PokemonRepository pokemon) {
		this.pokemon = pokemon;
	}

	public Mono<PokemonSpecimen> find(TrainerId trainer, PokemonId id) {
		return pokemon.findByOwner(trainer, id)
				.switchIfEmpty(Mono.error(() -> new PokemonNotFoundException(trainer, id)));
	}

}
