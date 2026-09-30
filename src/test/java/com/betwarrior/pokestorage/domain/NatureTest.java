package com.betwarrior.pokestorage.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NatureTest {

	@Test
	void givenTheNatureCatalog_whenCountingIt_thenThereAreTwentyFiveNaturesAndFiveAreNeutral() {
		Nature[] natures = Nature.values();

		assertThat(natures).hasSize(25);
		assertThat(Arrays.stream(natures).filter(Nature::isNeutral)).hasSize(5);
	}

	@Test
	void givenAnAdamantNature_whenAskingForModifiers_thenAttackRisesAndSpecialAttackDrops() {
		Nature nature = Nature.ADAMANT;

		assertThat(nature.modifierFor(Stat.ATTACK)).isEqualTo(1.1);
		assertThat(nature.modifierFor(Stat.SPECIAL_ATTACK)).isEqualTo(0.9);
		assertThat(nature.modifierFor(Stat.SPEED)).isEqualTo(1.0);
	}

	@ParameterizedTest
	@EnumSource(Nature.class)
	void givenAnyNature_whenAskingForTheHpModifier_thenHpIsNeverAffected(Nature nature) {
		double modifier = nature.modifierFor(Stat.HP);

		assertThat(modifier).isEqualTo(1.0);
	}

	@ParameterizedTest
	@EnumSource(value = Nature.class, names = { "HARDY", "DOCILE", "SERIOUS", "BASHFUL", "QUIRKY" })
	void givenANeutralNature_whenAskingForModifiers_thenNoStatChanges(Nature nature) {
		assertThat(Arrays.stream(Stat.values()).map(nature::modifierFor)).containsOnly(1.0);
	}

}
