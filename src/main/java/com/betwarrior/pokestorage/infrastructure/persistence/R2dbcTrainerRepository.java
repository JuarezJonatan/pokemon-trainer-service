package com.betwarrior.pokestorage.infrastructure.persistence;

import java.util.UUID;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.betwarrior.pokestorage.application.port.TrainerRepository;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import lombok.RequiredArgsConstructor;
import io.r2dbc.spi.Readable;
import reactor.core.publisher.Flux;
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
				.map(R2dbcTrainerRepository::toTrainer)
				.one();
	}

	@Override
	public Flux<Trainer> findAll(int offset, int limit) {
		return database.sql("select id, name from trainer order by created_at, id offset :offset limit :limit")
				.bind("offset", offset)
				.bind("limit", limit)
				.map(R2dbcTrainerRepository::toTrainer)
				.all();
	}

	@Override
	public Mono<Long> count() {
		return database.sql("select count(*) as total from trainer")
				.map(row -> row.get("total", Long.class))
				.one();
	}

	private static Trainer toTrainer(Readable row) {
		return new Trainer(new TrainerId(row.get("id", UUID.class)), row.get("name", String.class));
	}

}
