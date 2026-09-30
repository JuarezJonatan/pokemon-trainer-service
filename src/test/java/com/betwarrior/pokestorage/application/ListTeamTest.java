package com.betwarrior.pokestorage.application;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.eevee;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.pikachu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.specimenOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.domain.StorageSlot;
import com.betwarrior.pokestorage.domain.Trainer;
import com.betwarrior.pokestorage.domain.TrainerId;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonCatalog;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonRepository;
import com.betwarrior.pokestorage.testsupport.InMemoryTrainerRepository;
import com.betwarrior.pokestorage.testsupport.PokemonFixtures;

import reactor.test.StepVerifier;

class ListTeamTest {

	private final InMemoryTrainerRepository trainers = new InMemoryTrainerRepository();
	private final InMemoryPokemonRepository pokemon = new InMemoryPokemonRepository();
	private final ListTeam listTeam = new ListTeam(trainers, pokemon,
			new InMemoryPokemonCatalog().with(pikachu(), eevee()), new StorageProperties(6, 30));

	@Test
	void givenATeamWithGapsAndBoxedPokemon_whenListingTheTeam_thenOnlyTeamMembersAreReturnedInSlotOrderWithSpeciesData() {
		trainers.save(new Trainer(PokemonFixtures.ASH, "Ash")).block();
		pokemon.insert(specimenOf(eevee(), "run-away", StorageSlot.team(5))).block();
		pokemon.insert(specimenOf(pikachu(), "static", StorageSlot.team(2))).block();
		pokemon.insert(specimenOf(pikachu(), "static", StorageSlot.box(1))).block();

		List<TeamMember> team = listTeam.list(PokemonFixtures.ASH).block();

		assertThat(team).extracting(member -> member.pokemon().slot())
				.containsExactly(StorageSlot.team(2), StorageSlot.team(5));
		assertThat(team.get(0).species().types()).containsExactly("electric");
		assertThat(team.get(0).stats().hp()).isGreaterThan(team.get(0).species().baseStats().hp());
	}

	@Test
	void givenAnUnknownTrainer_whenListingTheTeam_thenItIsRejected() {
		StepVerifier.create(listTeam.list(TrainerId.random()))
				.expectError(TrainerNotFoundException.class)
				.verify();
	}

}
