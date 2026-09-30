package com.betwarrior.pokestorage.infrastructure.persistence;

import java.util.UUID;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class R2dbcTrainerRepository implements TrainerRepository {

	private final DatabaseClient database;

	@Override
	public Mono<Trainer> save(Trainer trainer) {
		return database.sql("insert into trainer (id, name) values (:id, :name)")
				.bind("id", trainer.id().value())
				.bind("name", trainer.name())
				.then()
				.thenReturn(trainer);
	}

	@Override
	public Mono<Trainer> findById(TrainerId id) {
		return database.sql("select id, name from trainer where id = :id")
				.bind("id", id.value())
				.map(row -> new Trainer(new TrainerId(row.get("id", UUID.class)), row.get("name", String.class)))
				.one();
	}

}
