package com.betwarrior.pokestorage.application.usecase.pokemon;

import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.magnemite;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.pikachu;
import static com.betwarrior.pokestorage.testsupport.PokemonFixtures.stats;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException;
import com.betwarrior.pokestorage.application.exception.InvalidItemException;
import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.exception.SpeciesRuleViolationException;
import com.betwarrior.pokestorage.domain.exception.StorageFullException;
import com.betwarrior.pokestorage.domain.pokemon.Gender;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.stats.StatValues;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.Trainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonCatalog;
import com.betwarrior.pokestorage.testsupport.InMemoryPokemonRepository;
import com.betwarrior.pokestorage.testsupport.InMemoryTrainerRepository;
import com.betwarrior.pokestorage.testsupport.PassThroughTransactions;

import reactor.test.StepVerifier;

class CapturePokemonTest {

	private static final Instant NOW = Instant.parse("2026-09-30T12:00:00Z");

	private final InMemoryTrainerRepository trainers = new InMemoryTrainerRepository();
	private final InMemoryPokemonRepository pokemon = new InMemoryPokemonRepository();
	private final InMemoryPokemonCatalog catalog = new InMemoryPokemonCatalog()
			.with(pikachu(), magnemite())
			.withItem("poke-ball", "standard-balls")
			.withItem("light-ball", "species-specific")
			.withItem("potion", "healing");
	private final CapturePokemon capturePokemon = new CapturePokemon(trainers, pokemon, catalog,
			new StorageProperties(2, 1), new PassThroughTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

	private Trainer ash;

	@BeforeEach
	void registerTrainer() {
		ash = trainers.save(Trainer.register("Ash")).block();
	}

	@Test
	void givenATrainerWithAnEmptyTeam_whenCapturingAValidPikachu_thenItIsStoredInTheFirstTeamSlot() {
		CapturePokemonCommand command = pikachuCapture();

		PokemonSpecimen captured = capturePokemon.capture(command).block();

		assertThat(captured.slot()).isEqualTo(StorageSlot.team(1));
		assertThat(captured.species().name()).isEqualTo("pikachu");
		assertThat(pokemon.stored(captured.id())).isEqualTo(captured);
	}

	@Test
	void givenMixedCaseNames_whenCapturing_thenTheyAreNormalizedToPokeApiNames() {
		CapturePokemonCommand command = pikachuCapture("  PIKACHU ", "Static", List.of("Thunderbolt"), "Poke-Ball");

		PokemonSpecimen captured = capturePokemon.capture(command).block();

		assertThat(captured.ability()).isEqualTo("static");
		assertThat(captured.moves().moves()).containsExactly("thunderbolt");
		assertThat(captured.origin().pokeball()).isEqualTo("poke-ball");
	}

	@Test
	void givenNoOriginalTrainerNorCaptureDate_whenCapturing_thenTheCapturingTrainerAndTheCurrentTimeAreRecorded() {
		CapturePokemonCommand command = pikachuCapture();

		PokemonSpecimen captured = capturePokemon.capture(command).block();

		assertThat(captured.origin().originalTrainerId()).isEqualTo(ash.id().toString());
		assertThat(captured.origin().caughtAt()).isEqualTo(NOW);
		assertThat(captured.origin().metLevel()).isEqualTo(captured.level());
		assertThat(captured.effortValues().values().total()).isZero();
	}

	@Test
	void givenAFullTeam_whenCapturing_thenThePokemonIsSentToTheBox() {
		capturePokemon.capture(pikachuCapture()).block();
		capturePokemon.capture(pikachuCapture()).block();

		PokemonSpecimen third = capturePokemon.capture(pikachuCapture()).block();

		assertThat(third.slot()).isEqualTo(StorageSlot.box(1));
	}

	@Test
	void givenAFullTeamAndBox_whenCapturing_thenItIsRejected() {
		capturePokemon.capture(pikachuCapture()).block();
		capturePokemon.capture(pikachuCapture()).block();
		capturePokemon.capture(pikachuCapture()).block();

		StepVerifier.create(capturePokemon.capture(pikachuCapture()))
				.expectError(StorageFullException.class)
				.verify();
	}

	@Test
	void givenAnUnknownTrainer_whenCapturing_thenItIsRejected() {
		CapturePokemonCommand command = withTrainer(pikachuCapture(), TrainerId.random());

		StepVerifier.create(capturePokemon.capture(command))
				.expectError(TrainerNotFoundException.class)
				.verify();
	}

	@Test
	void givenAnAbilityForeignToTheSpecies_whenCapturing_thenItIsRejectedAndNothingIsStored() {
		CapturePokemonCommand command = pikachuCapture("pikachu", "intimidate", List.of("thunderbolt"), "poke-ball");

		StepVerifier.create(capturePokemon.capture(command))
				.expectError(SpeciesRuleViolationException.class)
				.verify();
		assertThat(pokemon.countInArea(ash.id(), StorageArea.TEAM).block()).isZero();
	}

	@Test
	void givenAnItemThatIsNotAPokeball_whenCapturing_thenItIsRejected() {
		CapturePokemonCommand command = pikachuCapture("pikachu", "static", List.of("thunderbolt"), "potion");

		StepVerifier.create(capturePokemon.capture(command))
				.expectErrorMatches(error -> error instanceof InvalidItemException
						&& error.getMessage().contains("potion"))
				.verify();
	}

	@Test
	void givenAnUnknownSpecies_whenCapturing_thenItIsRejected() {
		CapturePokemonCommand command = pikachuCapture("missingno", "static", List.of("thunderbolt"), "poke-ball");

		StepVerifier.create(capturePokemon.capture(command))
				.expectError(UnknownCatalogEntryException.class)
				.verify();
	}

	@Test
	void givenAnIndividualValueOutOfRange_whenCapturing_thenItIsRejectedWithoutQueryingPokeApi() {
		CapturePokemonCommand command = withIndividualValues(pikachuCapture(), new StatValues(32, 0, 0, 0, 0, 0));

		StepVerifier.create(capturePokemon.capture(command))
				.expectError(InvalidValueException.class)
				.verify();
		assertThat(catalog.lookups()).isZero();
	}

	@Test
	void givenAConcurrentCaptureTakingTheSameSlot_whenCapturing_thenTheCaptureIsRetriedAndSucceeds() {
		pokemon.simulateConcurrentSlotConflicts(2);

		PokemonSpecimen captured = capturePokemon.capture(pikachuCapture()).block();

		assertThat(pokemon.stored(captured.id())).isNotNull();
	}

	@Test
	void givenAsManyLostRacesAsThereAreSlots_whenCapturing_thenItKeepsRetryingAndSucceeds() {
		pokemon.simulateConcurrentSlotConflicts(3);

		PokemonSpecimen captured = capturePokemon.capture(pikachuCapture()).block();

		assertThat(pokemon.stored(captured.id())).isNotNull();
	}

	@Test
	void givenMoreLostRacesThanSlots_whenCapturing_thenAConcurrentUpdateIsReported() {
		pokemon.simulateConcurrentSlotConflicts(4);

		StepVerifier.create(capturePokemon.capture(pikachuCapture()))
				.expectError(SlotAlreadyTakenException.class)
				.verify();
	}

	private CapturePokemonCommand pikachuCapture() {
		return pikachuCapture("pikachu", "static", List.of("thunderbolt", "quick-attack"), "poke-ball");
	}

	private CapturePokemonCommand pikachuCapture(String species, String ability, List<String> moves, String pokeball) {
		return new CapturePokemonCommand(ash.id(), species, Optional.of("Sparky"), 12, stats(20), Optional.empty(),
				Nature.TIMID, ability, Gender.MALE, false, Optional.empty(), pokeball, Optional.empty(),
				"viridian-forest", moves, Optional.of("light-ball"));
	}

	private static CapturePokemonCommand withTrainer(CapturePokemonCommand c, TrainerId trainer) {
		return new CapturePokemonCommand(trainer, c.species(), c.nickname(), c.level(), c.individualValues(),
				c.effortValues(), c.nature(), c.ability(), c.gender(), c.shiny(), c.originalTrainerId(), c.pokeball(),
				c.caughtAt(), c.metLocation(), c.moves(), c.heldItem());
	}

	private static CapturePokemonCommand withIndividualValues(CapturePokemonCommand c, StatValues ivs) {
		return new CapturePokemonCommand(c.trainer(), c.species(), c.nickname(), c.level(), ivs, c.effortValues(),
				c.nature(), c.ability(), c.gender(), c.shiny(), c.originalTrainerId(), c.pokeball(), c.caughtAt(),
				c.metLocation(), c.moves(), c.heldItem());
	}

}
