package com.betwarrior.pokestorage.application;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.domain.InvalidValueException;
import com.betwarrior.pokestorage.domain.StorageArea;
import com.betwarrior.pokestorage.domain.TrainerId;

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
