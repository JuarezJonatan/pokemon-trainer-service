package com.betwarrior.pokestorage.application.usecase.storage;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Mono;

@Component
public class ListBox {

	public static final int MAX_PAGE_SIZE = 100;

	private final TrainerRepository trainers;
	private final PokemonRepository pokemon;

	public ListBox(TrainerRepository trainers, PokemonRepository pokemon) {
		this.trainers = trainers;
		this.pokemon = pokemon;
	}

	public Mono<BoxPage> list(TrainerId trainer, int page, int size) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			return Mono.error(new InvalidValueException(
					"page must be >= 0 and size between 1 and %d".formatted(MAX_PAGE_SIZE)));
		}
		return trainers.findById(trainer)
				.switchIfEmpty(Mono.error(() -> new TrainerNotFoundException(trainer)))
				.then(Mono.zip(
						pokemon.findInArea(trainer, StorageArea.BOX, page * size, size).collectList(),
						pokemon.countInArea(trainer, StorageArea.BOX)))
				.map(result -> new BoxPage(result.getT1(), page, size, result.getT2()));
	}

}
