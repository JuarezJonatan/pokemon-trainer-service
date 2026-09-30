package com.betwarrior.pokestorage.domain;

import static com.betwarrior.pokestorage.domain.DomainFixtures.eevee;
import static com.betwarrior.pokestorage.domain.DomainFixtures.gyarados;
import static com.betwarrior.pokestorage.domain.DomainFixtures.magikarp;
import static com.betwarrior.pokestorage.domain.DomainFixtures.magnemite;
import static com.betwarrior.pokestorage.domain.DomainFixtures.pikachu;
import static com.betwarrior.pokestorage.domain.DomainFixtures.vaporeon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SpeciesTest {

	@Test
	void givenAnAbilityMovesAndGenderAllowedBySpecies_whenValidating_thenNoViolationIsRaised() {
		Species pikachu = pikachu();

		assertThatCode(() -> pikachu.validateIndividual("static", new MoveSet(List.of("thunderbolt")), Gender.MALE))
				.doesNotThrowAnyException();
	}

	@Test
	void givenAnAbilityAMoveAndAGenderForeignToTheSpecies_whenValidating_thenAllViolationsAreReported() {
		Species magnemite = magnemite();

		assertThatThrownBy(() -> magnemite.validateIndividual("intimidate", new MoveSet(List.of("tackle", "surf")), Gender.MALE))
				.isInstanceOfSatisfying(SpeciesRuleViolationException.class, error -> assertThat(error.violations())
						.hasSize(3)
						.anySatisfy(violation -> assertThat(violation).contains("intimidate"))
						.anySatisfy(violation -> assertThat(violation).contains("surf"))
						.anySatisfy(violation -> assertThat(violation).contains("MALE")));
	}

	@Test
	void givenAHiddenAbility_whenValidating_thenItIsAllowed() {
		Species pikachu = pikachu();

		assertThatCode(() -> pikachu.validateIndividual("lightning-rod", new MoveSet(List.of("thunderbolt")), Gender.FEMALE))
				.doesNotThrowAnyException();
	}

	@Test
	void givenBranchedEvolutions_whenCheckingTheDirectPredecessor_thenOnlyTheImmediateAncestorMatches() {
		Species vaporeon = vaporeon();

		assertThat(vaporeon.evolvesDirectlyFrom(eevee().ref())).isTrue();
		assertThat(vaporeon.evolvesDirectlyFrom(pikachu().ref())).isFalse();
	}

	@Test
	void givenABaseSpecies_whenCheckingAnyPredecessor_thenNoneMatches() {
		Species magikarp = magikarp();

		assertThat(magikarp.evolvesDirectlyFrom(pikachu().ref())).isFalse();
	}

	@Test
	void givenMagikarpWithSwiftSwim_whenEvolvingIntoGyaradosWithoutChoosingAnAbility_thenItGetsIntimidate() {
		Species gyarados = gyarados();

		String ability = gyarados.abilityAfterEvolvingFrom(magikarp(), "swift-swim", Optional.empty());

		assertThat(ability).isEqualTo("intimidate");
	}

	@Test
	void givenAHiddenAbility_whenEvolvingWithoutChoosing_thenTheHiddenAbilityOfTheSameSlotIsKept() {
		Species gyarados = gyarados();

		String ability = gyarados.abilityAfterEvolvingFrom(magikarp(), "rattled", Optional.empty());

		assertThat(ability).isEqualTo("moxie");
	}

	@Test
	void givenAnAbilitySlotMissingInTheTarget_whenEvolvingWithoutChoosing_thenTheFirstRegularAbilityIsUsed() {
		Species vaporeon = vaporeon();

		String ability = vaporeon.abilityAfterEvolvingFrom(eevee(), "adaptability", Optional.empty());

		assertThat(ability).isEqualTo("water-absorb");
	}

	@Test
	void givenARequestedAbilityOfTheTarget_whenEvolving_thenTheRequestedAbilityIsUsed() {
		Species gyarados = gyarados();

		String ability = gyarados.abilityAfterEvolvingFrom(magikarp(), "swift-swim", Optional.of("moxie"));

		assertThat(ability).isEqualTo("moxie");
	}

	@Test
	void givenARequestedAbilityForeignToTheTarget_whenEvolving_thenItIsRejected() {
		Species gyarados = gyarados();

		assertThatThrownBy(() -> gyarados.abilityAfterEvolvingFrom(magikarp(), "swift-swim", Optional.of("swift-swim")))
				.isInstanceOf(SpeciesRuleViolationException.class)
				.hasMessageContaining("swift-swim");
	}

	@Test
	void givenTheReferenceGarchompFromTheGames_whenCalculatingItsStats_thenTheyMatchTheOfficialFormula() {
		Species garchomp = new Species(new SpeciesRef(445, "garchomp"), List.of(new SpeciesAbility("sand-veil", 1, false)),
				Set.of(), new GenderRatio(4), new StatValues(108, 130, 95, 80, 85, 102), List.of("dragon", "ground"),
				null, Optional.of("gabite"));
		IndividualValues ivs = new IndividualValues(new StatValues(24, 12, 30, 16, 23, 5));
		EffortValues evs = new EffortValues(new StatValues(74, 190, 91, 48, 84, 23));

		StatValues stats = garchomp.statsAt(new Level(78), ivs, evs, Nature.ADAMANT);

		assertThat(stats).isEqualTo(new StatValues(289, 278, 193, 135, 171, 171));
	}

}
