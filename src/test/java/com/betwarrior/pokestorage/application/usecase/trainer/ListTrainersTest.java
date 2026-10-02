package com.betwarrior.pokestorage.application.usecase.trainer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.testsupport.InMemoryTrainerRepository;

import reactor.test.StepVerifier;

class ListTrainersTest {

	private final InMemoryTrainerRepository trainers = new InMemoryTrainerRepository();
	private final ListTrainers listTrainers = new ListTrainers(trainers);

	@BeforeEach
	void registerFiveTrainers() {
		for (String name : new String[] { "Ash", "Misty", "Brock", "Gary", "May" }) {
			trainers.save(Trainer.register(name)).block();
		}
	}

	@Test
	void givenFiveTrainers_whenAskingForTheSecondPageOfTwo_thenTheThirdAndFourthRegisteredAreReturned() {
		Page<Trainer> page = listTrainers.list(1, 2).block();

		assertThat(page.content()).extracting(Trainer::name).containsExactly("Brock", "Gary");
		assertThat(page.totalElements()).isEqualTo(5);
		assertThat(page.totalPages()).isEqualTo(3);
	}

	@Test
	void givenAPageBeyondTheLast_whenListingTrainers_thenItIsEmptyButKeepsTheTotal() {
		Page<Trainer> page = listTrainers.list(9, 2).block();

		assertThat(page.content()).isEmpty();
		assertThat(page.totalElements()).isEqualTo(5);
	}

	@Test
	void givenANegativePage_whenListingTrainers_thenItIsRejected() {
		StepVerifier.create(listTrainers.list(-1, 2))
				.expectError(InvalidValueException.class)
				.verify();
	}

}
