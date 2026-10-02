package com.betwarrior.pokestorage.application.port;

import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainerRepository {

	Mono<Trainer> save(Trainer trainer);

	Mono<Trainer> findById(TrainerId id);

	/**
	 * Trainers in registration order.
	 */
	Flux<Trainer> findAll(int offset, int limit);

	Mono<Long> count();

}
