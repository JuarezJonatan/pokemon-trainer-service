package com.betwarrior.pokestorage.application;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.domain.Trainer;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Mono;

@Component
public class FindTrainer {

	private final TrainerRepository trainers;

	public FindTrainer(TrainerRepository trainers) {
		this.trainers = trainers;
	}

	public Mono<Trainer> find(TrainerId id) {
		return trainers.findById(id).switchIfEmpty(Mono.error(() -> new TrainerNotFoundException(id)));
	}

}
