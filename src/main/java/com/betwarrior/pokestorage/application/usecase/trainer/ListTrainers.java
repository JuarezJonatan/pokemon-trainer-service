package com.betwarrior.pokestorage.application.usecase.trainer;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.application.pagination.PageRequest;
import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.trainer.Trainer;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ListTrainers {

	private final TrainerRepository trainers;

	public Mono<Page<Trainer>> list(int page, int size) {
		return Mono.fromSupplier(() -> new PageRequest(page, size))
				.flatMap(request -> Mono.zip(trainers.findAll(request.offset(), request.size()).collectList(), trainers.count())
						.map(result -> Page.of(result.getT1(), request, result.getT2())));
	}

}
