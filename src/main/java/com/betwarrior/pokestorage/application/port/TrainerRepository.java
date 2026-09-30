package com.betwarrior.pokestorage.application.port;

import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Mono;

public interface TrainerRepository {

	Mono<Trainer> save(Trainer trainer);

	Mono<Trainer> findById(TrainerId id);

}
