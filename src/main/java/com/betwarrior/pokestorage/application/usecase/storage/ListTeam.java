package com.betwarrior.pokestorage.application.usecase.storage;

import java.util.List;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Mono;

/**
 * Active team as a composite view: each stored Pokemon combined with its species data from PokeAPI
 * and the stats resulting from both.
 */
@Component
public class ListTeam {

	private final TrainerRepository trainers;
	private final PokemonRepository pokemon;
	private final PokemonCatalog catalog;
	private final StorageProperties storage;

	public ListTeam(TrainerRepository trainers, PokemonRepository pokemon, PokemonCatalog catalog,
			StorageProperties storage) {
		this.trainers = trainers;
		this.pokemon = pokemon;
		this.catalog = catalog;
		this.storage = storage;
	}

	public Mono<List<TeamMember>> list(TrainerId trainer) {
		return trainers.findById(trainer)
				.switchIfEmpty(Mono.error(() -> new TrainerNotFoundException(trainer)))
				.thenMany(pokemon.findInArea(trainer, StorageArea.TEAM, 0, storage.teamCapacity()))
				.flatMapSequential(member -> catalog.findSpecies(member.species().name())
						.map(species -> TeamMember.of(member, species)))
				.collectList();
	}

}
