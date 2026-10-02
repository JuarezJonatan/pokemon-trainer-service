package com.betwarrior.pokestorage.application.usecase.pokemon;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.magikarp;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.ownedBy;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.pikachu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.specimenOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.application.pagination.PageRequest;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonRepository;

import reactor.test.StepVerifier;

class ListAllPokemonTest {

	private final InMemoryPokemonRepository pokemon = new InMemoryPokemonRepository();
	private final ListAllPokemon listAllPokemon = new ListAllPokemon(pokemon);

	@Test
	void givenPokemonOfTwoTrainersInTeamAndBox_whenListingAll_thenEveryOneIsPagedInTheOrderTheyWereStored() {
		PokemonSpecimen ashsPikachu = pokemon.insert(specimenOf(pikachu(), "static", StorageSlot.team(1))).block();
		PokemonSpecimen mistysMagikarp = pokemon.insert(
				ownedBy(specimenOf(magikarp(), "swift-swim", StorageSlot.box(1)), TrainerId.random())).block();
		PokemonSpecimen ashsBoxedPikachu = pokemon.insert(specimenOf(pikachu(), "static", StorageSlot.box(1))).block();

		Page<PokemonSpecimen> first = listAllPokemon.list(0, 2).block();
		Page<PokemonSpecimen> second = listAllPokemon.list(1, 2).block();

		assertThat(first.content()).containsExactly(ashsPikachu, mistysMagikarp);
		assertThat(second.content()).containsExactly(ashsBoxedPikachu);
		assertThat(first.totalElements()).isEqualTo(3);
		assertThat(first.totalPages()).isEqualTo(2);
	}

	@Test
	void givenNoPokemon_whenListingAll_thenThePageIsEmpty() {
		Page<PokemonSpecimen> page = listAllPokemon.list(0, 20).block();

		assertThat(page.content()).isEmpty();
		assertThat(page.totalPages()).isZero();
	}

	@Test
	void givenAPageSizeAboveTheLimit_whenListingAll_thenItIsRejected() {
		StepVerifier.create(listAllPokemon.list(0, PageRequest.MAX_SIZE + 1))
				.expectError(InvalidValueException.class)
				.verify();
	}

}
