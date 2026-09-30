package com.betwarrior.pokestorage.application.usecase.trainer;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.trainer.Trainer;

import reactor.core.publisher.Mono;

@Component
public class RegisterTrainer {

	private final TrainerRepository trainers;

	public RegisterTrainer(TrainerRepository trainers) {
		this.trainers = trainers;
	}

	public Mono<Trainer> register(String name) {
		return Mono.fromSupplier(() -> Trainer.register(name)).flatMap(trainers::save);
	}

}
