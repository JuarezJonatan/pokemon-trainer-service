package com.betwarrior.pokestorage.application.usecase.storage;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.application.pagination.PageRequest;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ListBox {

	private final TrainerRepository trainers;
	private final PokemonRepository pokemon;

	public Mono<Page<PokemonSpecimen>> list(TrainerId trainer, int page, int size) {
		return Mono.fromSupplier(() -> new PageRequest(page, size))
				.flatMap(request -> trainers.findById(trainer)
						.switchIfEmpty(Mono.error(() -> new TrainerNotFoundException(trainer)))
						.then(Mono.zip(
								pokemon.findInArea(trainer, StorageArea.BOX, request.offset(), request.size()).collectList(),
								pokemon.countInArea(trainer, StorageArea.BOX)))
						.map(result -> Page.of(result.getT1(), request, result.getT2())));
	}

}
