package com.betwarrior.pokestorage.testsupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.betwarrior.pokestorage.application.TrainerRepository;
import com.betwarrior.pokestorage.domain.Trainer;
import com.betwarrior.pokestorage.domain.TrainerId;

import reactor.core.publisher.Mono;

public class InMemoryTrainerRepository implements TrainerRepository {

	private final Map<TrainerId, Trainer> trainers = new ConcurrentHashMap<>();

	@Override
	public Mono<Trainer> save(Trainer trainer) {
		trainers.put(trainer.id(), trainer);
		return Mono.just(trainer);
	}

	@Override
	public Mono<Trainer> findById(TrainerId id) {
		return Mono.justOrEmpty(trainers.get(id));
	}

}
