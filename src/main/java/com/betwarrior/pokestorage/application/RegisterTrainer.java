package com.betwarrior.pokestorage.application;

import org.springframework.stereotype.Component;

import com.betwarrior.pokestorage.domain.Trainer;

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
