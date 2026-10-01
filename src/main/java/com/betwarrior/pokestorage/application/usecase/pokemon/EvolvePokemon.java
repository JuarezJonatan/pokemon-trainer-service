package com.betwarrior.pokestorage.application.usecase.pokemon;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.application.exception.PokemonNotFoundException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.application.usecase.storage.ConcurrentUpdateRetry;
import com.betwarrior.pokestorage.domain.exception.PokemonNotInTeamException;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Evolves a team member. If the Pokemon changes concurrently (e.g. it is deposited in the box meanwhile),
 * the evolution is retried from a fresh read, so it is either applied on top of the other change or rejected.
 */
@Component
@RequiredArgsConstructor
public class EvolvePokemon {

	private final PokemonRepository pokemon;
	private final PokemonCatalog catalog;
	private final StorageProperties storage;

	public Mono<PokemonSpecimen> evolve(TrainerId trainer, PokemonId id, String targetSpecies,
			Optional<String> ability) {
		return Mono.defer(() -> pokemon.findByOwner(trainer, id)
				.switchIfEmpty(Mono.error(() -> new PokemonNotFoundException(trainer, id)))
				.filter(PokemonSpecimen::isInTeam)
				.switchIfEmpty(Mono.error(() -> new PokemonNotInTeamException(id)))
				.flatMap(current -> Mono.zip(
								catalog.findSpecies(current.species().name()),
								catalog.findSpecies(CatalogNames.normalize(targetSpecies)))
						.map(species -> current.evolveInto(species.getT1(), species.getT2(), ability.map(CatalogNames::normalize))))
				.flatMap(pokemon::update))
				.retryWhen(ConcurrentUpdateRetry.boundedBy(storage.capacity()));
	}

}
