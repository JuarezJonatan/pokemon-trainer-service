package com.betwarrior.pokestorage.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GenderRatioTest {

	@Test
	void givenAGenderlessSpecies_whenCheckingGenders_thenOnlyGenderlessIsAllowed() {
		GenderRatio ratio = GenderRatio.GENDERLESS;

		assertThat(ratio.allows(Gender.GENDERLESS)).isTrue();
		assertThat(ratio.allows(Gender.MALE)).isFalse();
		assertThat(ratio.allows(Gender.FEMALE)).isFalse();
	}

	@Test
	void givenAnAllMaleSpecies_whenCheckingGenders_thenOnlyMaleIsAllowed() {
		GenderRatio ratio = new GenderRatio(0);

		assertThat(ratio.allows(Gender.MALE)).isTrue();
		assertThat(ratio.allows(Gender.FEMALE)).isFalse();
		assertThat(ratio.allows(Gender.GENDERLESS)).isFalse();
	}

	@Test
	void givenAnAllFemaleSpecies_whenCheckingGenders_thenOnlyFemaleIsAllowed() {
		GenderRatio ratio = new GenderRatio(8);

		assertThat(ratio.allows(Gender.FEMALE)).isTrue();
		assertThat(ratio.allows(Gender.MALE)).isFalse();
	}

	@Test
	void givenAMixedSpecies_whenCheckingGenders_thenBothSexesAreAllowed() {
		GenderRatio ratio = new GenderRatio(4);

		assertThat(ratio.allows(Gender.MALE)).isTrue();
		assertThat(ratio.allows(Gender.FEMALE)).isTrue();
		assertThat(ratio.allows(Gender.GENDERLESS)).isFalse();
	}

	@Test
	void givenARateOutsidePokeApiRange_whenCreatingTheRatio_thenItIsRejected() {
		assertThatThrownBy(() -> new GenderRatio(9)).isInstanceOf(InvalidValueException.class);
	}

}
