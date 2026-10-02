package com.betwarrior.pokestorage.testsupport;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryTrainerRepository implements TrainerRepository {

	private final Map<TrainerId, Trainer> trainers = new ConcurrentHashMap<>();
	private final Map<TrainerId, Long> registrationOrder = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong();

	@Override
	public Mono<Trainer> save(Trainer trainer) {
		trainers.put(trainer.id(), trainer);
		registrationOrder.putIfAbsent(trainer.id(), sequence.incrementAndGet());
		return Mono.just(trainer);
	}

	@Override
	public Mono<Trainer> findById(TrainerId id) {
		return Mono.justOrEmpty(trainers.get(id));
	}

	@Override
	public Flux<Trainer> findAll(int offset, int limit) {
		return Flux.fromStream(() -> trainers.values().stream()
				.sorted(Comparator.comparingLong(trainer -> registrationOrder.get(trainer.id())))
				.skip(offset)
				.limit(limit)
				.toList()
				.stream());
	}

	@Override
	public Mono<Long> count() {
		return Mono.fromSupplier(() -> (long) trainers.size());
	}

}
