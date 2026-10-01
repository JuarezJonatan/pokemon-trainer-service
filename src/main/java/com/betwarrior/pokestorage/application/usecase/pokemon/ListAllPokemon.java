package com.betwarrior.pokestorage.application.usecase.pokemon;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.application.pagination.PageRequest;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Every stored Pokemon, of every trainer and in any area.
 */
@Component
@RequiredArgsConstructor
public class ListAllPokemon {

	private final PokemonRepository pokemon;

	public Mono<Page<PokemonSpecimen>> list(int page, int size) {
		return Mono.fromSupplier(() -> new PageRequest(page, size))
				.flatMap(request -> Mono.zip(pokemon.findAll(request.offset(), request.size()).collectList(), pokemon.count())
						.map(result -> Page.of(result.getT1(), request, result.getT2())));
	}

}
