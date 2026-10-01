package com.betwarrior.pokestorage.infrastructure.persistence;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.gyarados;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.magikarp;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.ownedBy;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.pikachu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.specimenOf;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.withoutNicknameNorItem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;

import com.betwarrior.pokestorage.application.exception.PokemonChangedConcurrentlyException;
import com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;
import com.betwarrior.pokestorage.testsupport.PokemonFixtures;
import com.betwarrior.pokestorage.testsupport.PostgresContainerConfiguration;

import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({ PostgresContainerConfiguration.class, R2dbcPokemonRepository.class, R2dbcTrainerRepository.class })
class R2dbcPokemonRepositoryIT {

	@Autowired
	private R2dbcPokemonRepository repository;
	@Autowired
	private R2dbcTrainerRepository trainers;
	@Autowired
	private DatabaseClient database;

	@BeforeEach
	void cleanDatabaseAndRegisterAsh() {
		database.sql("delete from pokemon").then().then(database.sql("delete from trainer").then()).block();
		trainers.save(new Trainer(PokemonFixtures.ASH, "Ash")).block();
	}

	@Test
	void givenAStoredPokemon_whenReadingItBack_thenEveryAttributeIsPreserved() {
		PokemonSpecimen pikachu = specimenOf(pikachu(), "static", StorageSlot.team(3));
		repository.insert(pikachu).block();

		PokemonSpecimen found = repository.findByOwner(PokemonFixtures.ASH, pikachu.id()).block();

		assertThat(found).isEqualTo(pikachu);
	}

	@Test
	void givenAPokemonWithoutNicknameNorItem_whenReadingItBack_thenBothAreAbsent() {
		PokemonSpecimen plain = withoutNicknameNorItem(specimenOf(pikachu(), "static", StorageSlot.team(1)));
		repository.insert(plain).block();

		PokemonSpecimen found = repository.findByOwner(PokemonFixtures.ASH, plain.id()).block();

		assertThat(found.nickname()).isEmpty();
		assertThat(found.heldItem()).isEmpty();
	}

	@Test
	void givenAPokemonOfAnotherTrainer_whenLookingItUpAsAsh_thenItIsNotFound() {
		TrainerId misty = TrainerId.random();
		trainers.save(new Trainer(misty, "Misty")).block();
		PokemonSpecimen mistysPokemon = specimenOf(pikachu(), "static", StorageSlot.team(1));
		repository.insert(ownedBy(mistysPokemon, misty)).block();

		StepVerifier.create(repository.findByOwner(PokemonFixtures.ASH, mistysPokemon.id()))
				.verifyComplete();
	}

	@Test
	void givenAnOccupiedSlot_whenInsertingAnotherPokemonInIt_thenASlotConflictIsReported() {
		repository.insert(specimenOf(pikachu(), "static", StorageSlot.team(1))).block();

		StepVerifier.create(repository.insert(specimenOf(pikachu(), "static", StorageSlot.team(1))))
				.expectError(SlotAlreadyTakenException.class)
				.verify();
	}

	@Test
	void givenPokemonInTeamAndBox_whenQueryingByArea_thenResultsAreFilteredOrderedAndPaged() {
		repository.insert(specimenOf(pikachu(), "static", StorageSlot.box(2))).block();
		repository.insert(specimenOf(pikachu(), "static", StorageSlot.box(1))).block();
		repository.insert(specimenOf(pikachu(), "static", StorageSlot.box(3))).block();
		repository.insert(specimenOf(pikachu(), "static", StorageSlot.team(1))).block();

		var secondPage = repository.findInArea(PokemonFixtures.ASH, StorageArea.BOX, 1, 2).collectList().block();
		Long boxed = repository.countInArea(PokemonFixtures.ASH, StorageArea.BOX).block();
		Map<PokemonId, StorageSlot> occupied = repository.occupiedSlots(PokemonFixtures.ASH).block();

		assertThat(secondPage).extracting(pokemon -> pokemon.slot().position()).containsExactly(2, 3);
		assertThat(boxed).isEqualTo(3);
		assertThat(occupied).hasSize(4);
	}

	@Test
	void givenPokemonOfTwoTrainers_whenListingAll_thenEveryOneIsPagedInTheOrderTheyWereStored() {
		TrainerId misty = TrainerId.random();
		trainers.save(new Trainer(misty, "Misty")).block();
		PokemonSpecimen first = repository.insert(specimenOf(pikachu(), "static", StorageSlot.team(1))).block();
		PokemonSpecimen second = repository.insert(ownedBy(specimenOf(magikarp(), "swift-swim", StorageSlot.box(1)), misty)).block();
		PokemonSpecimen third = repository.insert(specimenOf(pikachu(), "static", StorageSlot.box(1))).block();

		var firstPage = repository.findAll(0, 2).collectList().block();
		var secondPage = repository.findAll(2, 2).collectList().block();

		assertThat(firstPage).containsExactly(first, second);
		assertThat(secondPage).containsExactly(third);
		assertThat(repository.count().block()).isEqualTo(3);
	}

	@Test
	void givenThreeTrainers_whenListingThem_thenTheyArePagedInRegistrationOrder() {
		Trainer misty = trainers.save(new Trainer(TrainerId.random(), "Misty")).block();
		Trainer brock = trainers.save(new Trainer(TrainerId.random(), "Brock")).block();

		var secondPage = trainers.findAll(1, 2).collectList().block();

		assertThat(secondPage).containsExactly(misty, brock);
		assertThat(trainers.count().block()).isEqualTo(3);
	}

	@Test
	void givenAnEvolvedPokemon_whenUpdatingIt_thenTheNewSpeciesAndAbilityArePersisted() {
		PokemonSpecimen magikarp = specimenOf(magikarp(), "swift-swim", StorageSlot.team(2));
		repository.insert(magikarp).block();
		PokemonSpecimen gyarados = magikarp.evolveInto(magikarp(), gyarados(), Optional.empty());

		PokemonSpecimen stored = repository.update(gyarados).block();

		assertThat(stored).isEqualTo(gyarados.withVersion(1));
		assertThat(repository.findByOwner(PokemonFixtures.ASH, magikarp.id()).block()).isEqualTo(stored);
	}

	@Test
	void givenAPokemonChangedAfterItWasRead_whenUpdatingTheStaleCopy_thenTheUpdateIsRejectedAndTheChangeKept() {
		PokemonSpecimen magikarp = repository.insert(specimenOf(magikarp(), "swift-swim", StorageSlot.team(1))).block();
		PokemonSpecimen deposited = repository.update(magikarp.storedAt(StorageSlot.box(1))).block();

		StepVerifier.create(repository.update(magikarp.evolveInto(magikarp(), gyarados(), Optional.empty())))
				.expectError(PokemonChangedConcurrentlyException.class)
				.verify();
		assertThat(repository.findByOwner(PokemonFixtures.ASH, magikarp.id()).block()).isEqualTo(deposited);
	}

}
