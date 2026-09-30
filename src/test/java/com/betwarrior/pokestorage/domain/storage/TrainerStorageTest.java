package com.betwarrior.pokestorage.domain.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.domain.exception.StorageFullException;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;

class TrainerStorageTest {

	private static final StorageCapacity SMALL = new StorageCapacity(2, 2);

	@Test
	void givenAnEmptyStorage_whenCapturing_thenThePokemonGoesToTheFirstTeamSlot() {
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of());

		StorageSlot slot = storage.slotForNewCapture();

		assertThat(slot).isEqualTo(StorageSlot.team(1));
	}

	@Test
	void givenAGapInTheTeam_whenCapturing_thenTheGapIsFilledFirst() {
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of(PokemonId.random(), StorageSlot.team(2)));

		StorageSlot slot = storage.slotForNewCapture();

		assertThat(slot).isEqualTo(StorageSlot.team(1));
	}

	@Test
	void givenAFullTeam_whenCapturing_thenThePokemonGoesToTheBox() {
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of(
				PokemonId.random(), StorageSlot.team(1),
				PokemonId.random(), StorageSlot.team(2)));

		StorageSlot slot = storage.slotForNewCapture();

		assertThat(slot).isEqualTo(StorageSlot.box(1));
	}

	@Test
	void givenAFullTeamAndBox_whenCapturing_thenItIsRejected() {
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of(
				PokemonId.random(), StorageSlot.team(1),
				PokemonId.random(), StorageSlot.team(2),
				PokemonId.random(), StorageSlot.box(1),
				PokemonId.random(), StorageSlot.box(2)));

		assertThatThrownBy(storage::slotForNewCapture)
				.isInstanceOf(StorageFullException.class)
				.hasMessageContaining("Both");
	}

	@Test
	void givenAPokemonInTheTeam_whenDepositingItInTheBox_thenItTakesTheFirstFreeBoxSlot() {
		PokemonId pikachu = PokemonId.random();
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of(
				pikachu, StorageSlot.team(1),
				PokemonId.random(), StorageSlot.box(1)));

		StorageSlot slot = storage.slotForTransfer(pikachu, StorageArea.BOX);

		assertThat(slot).isEqualTo(StorageSlot.box(2));
	}

	@Test
	void givenAPokemonAlreadyInTheDestination_whenTransferring_thenItKeepsItsSlot() {
		PokemonId pikachu = PokemonId.random();
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of(pikachu, StorageSlot.team(2)));

		StorageSlot slot = storage.slotForTransfer(pikachu, StorageArea.TEAM);

		assertThat(slot).isEqualTo(StorageSlot.team(2));
	}

	@Test
	void givenAFullTeam_whenWithdrawingFromTheBox_thenItIsRejectedNamingTheTeam() {
		PokemonId boxed = PokemonId.random();
		Map<PokemonId, StorageSlot> occupied = new HashMap<>();
		occupied.put(PokemonId.random(), StorageSlot.team(1));
		occupied.put(PokemonId.random(), StorageSlot.team(2));
		occupied.put(boxed, StorageSlot.box(1));
		TrainerStorage storage = new TrainerStorage(SMALL, occupied);

		assertThatThrownBy(() -> storage.slotForTransfer(boxed, StorageArea.TEAM))
				.isInstanceOfSatisfying(StorageFullException.class,
						error -> assertThat(error.area()).isEqualTo(StorageArea.TEAM));
	}

	@Test
	void givenAPokemonOfAnotherTrainer_whenTransferring_thenItIsRejected() {
		TrainerStorage storage = new TrainerStorage(SMALL, Map.of());

		assertThatThrownBy(() -> storage.slotForTransfer(PokemonId.random(), StorageArea.BOX))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
