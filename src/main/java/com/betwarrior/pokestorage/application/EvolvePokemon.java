package com.betwarrior.pokestorage.application;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.PokemonNotInTeamException;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Mono;

@Component
public class EvolvePokemon {

	private final PokemonRepository pokemon;
	private final PokemonCatalog catalog;

	public EvolvePokemon(PokemonRepository pokemon, PokemonCatalog catalog) {
		this.pokemon = pokemon;
		this.catalog = catalog;
	}

	public Mono<PokemonSpecimen> evolve(TrainerId trainer, PokemonId id, String targetSpecies,
			Optional<String> ability) {
		return pokemon.findByOwner(trainer, id)
				.switchIfEmpty(Mono.error(() -> new PokemonNotFoundException(trainer, id)))
				.filter(PokemonSpecimen::isInTeam)
				.switchIfEmpty(Mono.error(() -> new PokemonNotInTeamException(id)))
				.flatMap(current -> Mono.zip(
								catalog.findSpecies(current.species().name()),
								catalog.findSpecies(CatalogNames.normalize(targetSpecies)))
						.map(species -> current.evolveInto(species.getT1(), species.getT2(), ability.map(CatalogNames::normalize))))
				.flatMap(pokemon::update);
	}

}
