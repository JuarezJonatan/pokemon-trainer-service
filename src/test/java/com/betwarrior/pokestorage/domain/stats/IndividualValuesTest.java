package com.betwarrior.pokestorage.domain.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

class IndividualValuesTest {

	@Test
	void givenAllStatsWithinZeroAndThirtyOne_whenCreatingIndividualValues_thenTheyAreAccepted() {
		StatValues values = new StatValues(0, 31, 15, 1, 30, 31);

		IndividualValues ivs = new IndividualValues(values);

		assertThat(ivs.of(Stat.ATTACK)).isEqualTo(31);
		assertThat(ivs.of(Stat.HP)).isZero();
	}

	@Test
	void givenAStatAboveThirtyOne_whenCreatingIndividualValues_thenItIsRejected() {
		StatValues values = new StatValues(31, 31, 32, 31, 31, 31);

		assertThatThrownBy(() -> new IndividualValues(values))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("DEFENSE");
	}

	@Test
	void givenANegativeStat_whenCreatingIndividualValues_thenItIsRejected() {
		StatValues values = new StatValues(-1, 0, 0, 0, 0, 0);

		assertThatThrownBy(() -> new IndividualValues(values))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("HP");
	}

}
