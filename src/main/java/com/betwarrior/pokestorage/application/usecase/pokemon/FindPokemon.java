package com.betwarrior.pokestorage.application.usecase.pokemon;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.exception.PokemonNotFoundException;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

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
