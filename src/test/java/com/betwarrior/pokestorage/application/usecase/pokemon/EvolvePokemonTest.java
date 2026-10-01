package com.betwarrior.pokestorage.application.usecase.pokemon;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.eevee;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.gyarados;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.magikarp;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.raichu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.specimenOf;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.vaporeon;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.domain.exception.EvolutionNotAllowedException;
import com.betwarrior.pokestorage.domain.exception.PokemonNotInTeamException;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonCatalog;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonRepository;
import com.betwarrior.pokestorage.testsupport.PokemonFixtures;

import reactor.test.StepVerifier;

class EvolvePokemonTest {

	private final InMemoryPokemonRepository pokemon = new InMemoryPokemonRepository();
	private final InMemoryPokemonCatalog catalog = new InMemoryPokemonCatalog()
			.with(magikarp(), gyarados(), eevee(), vaporeon(), raichu());
	private final EvolvePokemon evolvePokemon = new EvolvePokemon(pokemon, catalog, new StorageProperties(6, 30));

	@Test
	void givenAMagikarpInTeamSlotFour_whenEvolvingIntoGyarados_thenItIsPersistedWithItsNewAbilityInTheSameSlot() {
		PokemonSpecimen magikarp = pokemon.insert(specimenOf(magikarp(), "swift-swim", StorageSlot.team(4))).block();

		PokemonSpecimen evolved = evolvePokemon.evolve(PokemonFixtures.ASH, magikarp.id(), "Gyarados", Optional.empty()).block();

		assertThat(pokemon.stored(magikarp.id())).isEqualTo(evolved);
		assertThat(evolved.species().name()).isEqualTo("gyarados");
		assertThat(evolved.ability()).isEqualTo("intimidate");
		assertThat(evolved.slot()).isEqualTo(StorageSlot.team(4));
	}

	@Test
	void givenADepositCommittedWhileEvolving_whenTheEvolutionIsStored_thenItIsRetriedAndRejectedBecauseThePokemonLeftTheTeam() {
		PokemonSpecimen magikarp = pokemon.insert(specimenOf(magikarp(), "swift-swim", StorageSlot.team(1))).block();
		pokemon.simulateConcurrentChange(current -> current.storedAt(StorageSlot.box(1)));

		StepVerifier.create(evolvePokemon.evolve(PokemonFixtures.ASH, magikarp.id(), "gyarados", Optional.empty()))
				.expectError(PokemonNotInTeamException.class)
				.verify();
		assertThat(pokemon.stored(magikarp.id()).species().name()).isEqualTo("magikarp");
		assertThat(pokemon.stored(magikarp.id()).slot()).isEqualTo(StorageSlot.box(1));
	}

	@Test
	void givenAMoveToAnotherTeamSlotCommittedWhileEvolving_whenTheEvolutionIsStored_thenItIsRetriedOnTopOfIt() {
		PokemonSpecimen magikarp = pokemon.insert(specimenOf(magikarp(), "swift-swim", StorageSlot.team(1))).block();
		pokemon.simulateConcurrentChange(current -> current.storedAt(StorageSlot.team(3)));

		PokemonSpecimen evolved = evolvePokemon.evolve(PokemonFixtures.ASH, magikarp.id(), "gyarados", Optional.empty()).block();

		assertThat(evolved.species().name()).isEqualTo("gyarados");
		assertThat(evolved.slot()).isEqualTo(StorageSlot.team(3));
		assertThat(pokemon.stored(magikarp.id())).isEqualTo(evolved);
	}

	@Test
	void givenAPokemonInTheBox_whenEvolving_thenItIsRejectedWithoutQueryingPokeApi() {
		PokemonSpecimen eevee = pokemon.insert(specimenOf(eevee(), "run-away", StorageSlot.box(1))).block();

		StepVerifier.create(evolvePokemon.evolve(PokemonFixtures.ASH, eevee.id(), "vaporeon", Optional.empty()))
				.expectError(PokemonNotInTeamException.class)
				.verify();
		assertThat(catalog.lookups()).isZero();
	}

	@Test
	void givenAnEevee_whenEvolvingIntoASpeciesOfAnotherLine_thenItIsRejectedAndNotChanged() {
		PokemonSpecimen eevee = pokemon.insert(specimenOf(eevee(), "run-away", StorageSlot.team(1))).block();

		StepVerifier.create(evolvePokemon.evolve(PokemonFixtures.ASH, eevee.id(), "raichu", Optional.empty()))
				.expectError(EvolutionNotAllowedException.class)
				.verify();
		assertThat(pokemon.stored(eevee.id())).isEqualTo(eevee);
	}

}
