package com.betwarrior.pokestorage.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EffortValuesTest {

	@Test
	void givenTwoMaxedStatsAndTheRemainderInAThird_whenCreatingEffortValues_thenTheyAreAccepted() {
		StatValues values = new StatValues(252, 252, 6, 0, 0, 0);

		EffortValues evs = new EffortValues(values);

		assertThat(evs.values().total()).isEqualTo(EffortValues.MAX_TOTAL);
	}

	@Test
	void givenAStatAbove252_whenCreatingEffortValues_thenItIsRejected() {
		StatValues values = new StatValues(0, 253, 0, 0, 0, 0);

		assertThatThrownBy(() -> new EffortValues(values))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("ATTACK");
	}

	@Test
	void givenATotalAbove510_whenCreatingEffortValues_thenItIsRejected() {
		StatValues values = new StatValues(252, 252, 7, 0, 0, 0);

		assertThatThrownBy(() -> new EffortValues(values))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("510");
	}

	@Test
	void givenANegativeStat_whenCreatingEffortValues_thenItIsRejected() {
		StatValues values = new StatValues(0, 0, 0, 0, 0, -4);

		assertThatThrownBy(() -> new EffortValues(values))
				.isInstanceOf(InvalidValueException.class);
	}

	@Test
	void givenANewlyCaughtPokemon_whenAskingForNoEffortValues_thenAllStatsAreZero() {
		EffortValues evs = EffortValues.none();

		assertThat(evs.values().total()).isZero();
	}

}
