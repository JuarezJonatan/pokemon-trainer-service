package com.betwarrior.pokestorage.domain;

import static com.betwarrior.pokestorage.domain.DomainFixtures.eevee;
import static com.betwarrior.pokestorage.domain.DomainFixtures.gyarados;
import static com.betwarrior.pokestorage.domain.DomainFixtures.magikarp;
import static com.betwarrior.pokestorage.domain.DomainFixtures.pikachu;
import static com.betwarrior.pokestorage.domain.DomainFixtures.raichu;
import static com.betwarrior.pokestorage.domain.DomainFixtures.specimenOf;
import static com.betwarrior.pokestorage.domain.DomainFixtures.vaporeon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class PokemonSpecimenTest {

	@Test
	void givenAMagikarpInTeamSlotThree_whenEvolvingIntoGyarados_thenOnlySpeciesAndAbilityChange() {
		PokemonSpecimen magikarp = specimenOf(magikarp(), "swift-swim", StorageSlot.team(3));

		PokemonSpecimen gyarados = magikarp.evolveInto(magikarp(), gyarados(), Optional.empty());

		assertThat(gyarados.species()).isEqualTo(gyarados().ref());
		assertThat(gyarados.ability()).isEqualTo("intimidate");
		assertThat(gyarados)
				.usingRecursiveComparison()
				.ignoringFields("species", "ability")
				.isEqualTo(magikarp);
	}

	@Test
	void givenAnEevee_whenEvolvingIntoOneOfItsBranches_thenTheEvolutionIsAccepted() {
		PokemonSpecimen eevee = specimenOf(eevee(), "run-away", StorageSlot.team(1));

		PokemonSpecimen vaporeon = eevee.evolveInto(eevee(), vaporeon(), Optional.empty());

		assertThat(vaporeon.species().name()).isEqualTo("vaporeon");
		assertThat(vaporeon.slot()).isEqualTo(StorageSlot.team(1));
	}

	@Test
	void givenAPokemon_whenEvolvingIntoASpeciesOutsideItsLine_thenItIsRejected() {
		PokemonSpecimen eevee = specimenOf(eevee(), "run-away", StorageSlot.team(1));

		assertThatThrownBy(() -> eevee.evolveInto(eevee(), raichu(), Optional.empty()))
				.isInstanceOf(EvolutionNotAllowedException.class)
				.hasMessageContaining("raichu is not a direct evolution of eevee");
	}

	@Test
	void givenAPokemonInTheBox_whenEvolving_thenItIsRejectedBecauseItIsNotInTheTeam() {
		PokemonSpecimen pikachu = specimenOf(pikachu(), "static", StorageSlot.box(4));

		assertThatThrownBy(() -> pikachu.evolveInto(pikachu(), raichu(), Optional.empty()))
				.isInstanceOf(PokemonNotInTeamException.class);
	}

	@Test
	void givenAPokemonInTheTeam_whenStoringItInTheBox_thenOnlyItsSlotChanges() {
		PokemonSpecimen pikachu = specimenOf(pikachu(), "static", StorageSlot.team(2));

		PokemonSpecimen boxed = pikachu.storedAt(StorageSlot.box(7));

		assertThat(boxed.isInTeam()).isFalse();
		assertThat(boxed).usingRecursiveComparison().ignoringFields("slot").isEqualTo(pikachu);
	}

	@Test
	void givenABlankAbility_whenCreatingASpecimen_thenItIsRejected() {
		assertThatThrownBy(() -> specimenOf(pikachu(), " ", StorageSlot.team(1)))
				.isInstanceOf(InvalidValueException.class);
	}

}
