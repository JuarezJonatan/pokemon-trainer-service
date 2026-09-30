package com.betwarrior.pokestorage.application;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.pikachu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.specimenOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.StorageArea;
import com.betwarrior.pokestorage.domain.StorageFullException;
import com.betwarrior.pokestorage.domain.StorageSlot;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonRepository;
import com.betwarrior.pokestorage.testsupport.PassThroughTransactions;
import com.betwarrior.pokestorage.testsupport.PokemonFixtures;

import reactor.test.StepVerifier;

class TransferPokemonTest {

	private final InMemoryPokemonRepository pokemon = new InMemoryPokemonRepository();
	private final TransferPokemon transferPokemon = new TransferPokemon(pokemon, new StorageProperties(2, 2),
			new PassThroughTransactions());

	@Test
	void givenAPokemonInTheTeam_whenDepositingItInTheBox_thenItTakesTheFirstFreeBoxSlot() {
		PokemonSpecimen pikachu = store(StorageSlot.team(1));

		PokemonSpecimen moved = transferPokemon.transfer(PokemonFixtures.ASH, pikachu.id(), StorageArea.BOX).block();

		assertThat(moved.slot()).isEqualTo(StorageSlot.box(1));
		assertThat(pokemon.stored(pikachu.id()).slot()).isEqualTo(StorageSlot.box(1));
	}

	@Test
	void givenAGapInTheTeam_whenWithdrawingFromTheBox_thenTheGapIsFilledAndOtherSlotsAreKept() {
		PokemonSpecimen second = store(StorageSlot.team(2));
		PokemonSpecimen boxed = store(StorageSlot.box(1));

		PokemonSpecimen moved = transferPokemon.transfer(PokemonFixtures.ASH, boxed.id(), StorageArea.TEAM).block();

		assertThat(moved.slot()).isEqualTo(StorageSlot.team(1));
		assertThat(pokemon.stored(second.id()).slot()).isEqualTo(StorageSlot.team(2));
	}

	@Test
	void givenAFullTeam_whenWithdrawingFromTheBox_thenItIsRejected() {
		store(StorageSlot.team(1));
		store(StorageSlot.team(2));
		PokemonSpecimen boxed = store(StorageSlot.box(1));

		StepVerifier.create(transferPokemon.transfer(PokemonFixtures.ASH, boxed.id(), StorageArea.TEAM))
				.expectError(StorageFullException.class)
				.verify();
	}

	@Test
	void givenAPokemonAlreadyInTheDestination_whenTransferring_thenNothingChanges() {
		PokemonSpecimen pikachu = store(StorageSlot.team(2));

		PokemonSpecimen result = transferPokemon.transfer(PokemonFixtures.ASH, pikachu.id(), StorageArea.TEAM).block();

		assertThat(result).isEqualTo(pikachu);
	}

	@Test
	void givenAnUnknownPokemon_whenTransferring_thenItIsRejected() {
		StepVerifier.create(transferPokemon.transfer(PokemonFixtures.ASH, PokemonId.random(), StorageArea.BOX))
				.expectError(PokemonNotFoundException.class)
				.verify();
	}

	private PokemonSpecimen store(StorageSlot slot) {
		return pokemon.insert(specimenOf(pikachu(), "static", slot)).block();
	}

}
