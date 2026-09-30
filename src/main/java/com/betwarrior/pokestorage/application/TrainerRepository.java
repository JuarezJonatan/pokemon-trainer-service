package com.betwarrior.pokestorage.application;

import com.betwarrior.pokestorage.domain.Trainer;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Mono;

public interface TrainerRepository {

	Mono<Trainer> save(Trainer trainer);

	Mono<Trainer> findById(TrainerId id);

}
