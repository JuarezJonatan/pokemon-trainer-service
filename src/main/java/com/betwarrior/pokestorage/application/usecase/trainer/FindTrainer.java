package com.betwarrior.pokestorage.application.usecase.trainer;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

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
