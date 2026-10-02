package com.betwarrior.pokestorage.application.usecase.storage;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.pikachu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.specimenOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.application.pagination.PageRequest;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonRepository;
import com.betwarrior.pokestorage.testsupport.InMemoryTrainerRepository;
import com.betwarrior.pokestorage.testsupport.PokemonFixtures;

import reactor.test.StepVerifier;

class ListBoxTest {

	private final InMemoryTrainerRepository trainers = new InMemoryTrainerRepository();
	private final InMemoryPokemonRepository pokemon = new InMemoryPokemonRepository();
	private final ListBox listBox = new ListBox(trainers, pokemon);

	@BeforeEach
	void storeFiveBoxedPokemon() {
		trainers.save(new Trainer(PokemonFixtures.ASH, "Ash")).block();
		pokemon.insert(specimenOf(pikachu(), "static", StorageSlot.team(1))).block();
		for (int position = 1; position <= 5; position++) {
			pokemon.insert(specimenOf(pikachu(), "static", StorageSlot.box(position))).block();
		}
	}

	@Test
	void givenFiveBoxedPokemon_whenAskingForTheSecondPageOfTwo_thenSlotsThreeAndFourAreReturned() {
		Page<PokemonSpecimen> page = listBox.list(PokemonFixtures.ASH, 1, 2).block();

		assertThat(page.content()).extracting(specimen -> specimen.slot().position()).containsExactly(3, 4);
		assertThat(page.totalElements()).isEqualTo(5);
		assertThat(page.totalPages()).isEqualTo(3);
	}

	@Test
	void givenAPageSizeAboveTheLimit_whenListingTheBox_thenItIsRejected() {
		StepVerifier.create(listBox.list(PokemonFixtures.ASH, 0, PageRequest.MAX_SIZE + 1))
				.expectError(InvalidValueException.class)
				.verify();
	}

}
