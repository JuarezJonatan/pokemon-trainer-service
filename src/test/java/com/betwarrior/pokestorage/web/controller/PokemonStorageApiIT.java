package com.betwarrior.pokestorage.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.betwarrior.pokestorage.testsupport.PokeApiStub;
import com.betwarrior.pokestorage.testsupport.PostgresContainerConfiguration;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"pokestorage.storage.team-capacity=6",
		"pokestorage.storage.box-capacity=2" })
@AutoConfigureWebTestClient
@Import(PostgresContainerConfiguration.class)
class PokemonStorageApiIT {

	private static final PokeApiStub POKE_API = new PokeApiStub();

	@DynamicPropertySource
	static void pointToPokeApiStub(DynamicPropertyRegistry registry) {
		registry.add("pokeapi.base-url", POKE_API::baseUrl);
	}

	@AfterAll
	static void stopPokeApi() throws Exception {
		POKE_API.close();
	}

	@Autowired
	private WebTestClient http;

	private String trainer;

	@BeforeEach
	void registerTrainer() {
		trainer = http.post().uri("/api/v1/trainers").bodyValue(Map.of("name", "Ash"))
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().value("Location", location -> assertThat(location).contains("/api/v1/trainers/"))
				.expectBody(Map.class).returnResult().getResponseBody().get("id").toString();
	}

	@Test
	void givenANewTrainer_whenCapturingSevenPokemon_thenSixFillTheTeamAndTheSeventhGoesToTheBox() {
		List<Map<String, Object>> captured = IntStream.range(0, 7).mapToObj(i -> capture(pikachu())).toList();

		assertThat(captured).extracting(pokemon -> storage(pokemon).get("area"))
				.containsExactly("TEAM", "TEAM", "TEAM", "TEAM", "TEAM", "TEAM", "BOX");
		http.get().uri("/api/v1/trainers/{t}/box", trainer).exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.totalElements").isEqualTo(1)
				.jsonPath("$.pokemon[0].storage.slot").isEqualTo(1);
	}

	@Test
	void givenACapturedPokemon_whenRequestingItsDetail_thenGeneticsTrainingAndOriginAreReturned() {
		Map<String, Object> pikachu = capture(pikachu());

		http.get().uri("/api/v1/trainers/{t}/pokemon/{p}", trainer, pikachu.get("id")).exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.species.name").isEqualTo("pikachu")
				.jsonPath("$.species.id").isEqualTo(25)
				.jsonPath("$.nickname").isEqualTo("Sparky")
				.jsonPath("$.individualValues.speed").isEqualTo(31)
				.jsonPath("$.effortValues.specialAttack").isEqualTo(252)
				.jsonPath("$.nature.name").isEqualTo("TIMID")
				.jsonPath("$.nature.increased").isEqualTo("SPEED")
				.jsonPath("$.ability").isEqualTo("static")
				.jsonPath("$.shiny").isEqualTo(true)
				.jsonPath("$.moves.length()").isEqualTo(2)
				.jsonPath("$.heldItem").isEqualTo("light-ball")
				.jsonPath("$.origin.pokeball").isEqualTo("poke-ball")
				.jsonPath("$.origin.originalTrainerId").isEqualTo(trainer)
				.jsonPath("$.origin.metLevel").isEqualTo(25);
	}

	@Test
	void givenATeam_whenListingIt_thenEachMemberCombinesStoredDataWithPokeApiSpeciesAndCalculatedStats() {
		capture(pikachu());
		capture(magikarp());

		http.get().uri("/api/v1/trainers/{t}/team", trainer).exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.capacity").isEqualTo(6)
				.jsonPath("$.members.length()").isEqualTo(2)
				.jsonPath("$.members[0].slot").isEqualTo(1)
				.jsonPath("$.members[0].pokemon.ability").isEqualTo("static")
				.jsonPath("$.members[0].species.types[0]").isEqualTo("electric")
				.jsonPath("$.members[0].species.baseStats.speed").isEqualTo(90)
				.jsonPath("$.members[0].species.spriteUrl").isNotEmpty()
				.jsonPath("$.members[0].stats.hp").isEqualTo(60)
				.jsonPath("$.members[1].species.name").isEqualTo("magikarp");
	}

	@Test
	void givenAPokemonInTheTeam_whenDepositingAndWithdrawingIt_thenItMovesBetweenTeamAndBox() {
		Map<String, Object> pikachu = capture(pikachu());

		transfer(pikachu.get("id"), "BOX").expectStatus().isOk()
				.expectBody().jsonPath("$.storage.area").isEqualTo("BOX");
		transfer(pikachu.get("id"), "TEAM").expectStatus().isOk()
				.expectBody().jsonPath("$.storage.area").isEqualTo("TEAM")
				.jsonPath("$.storage.slot").isEqualTo(1);
	}

	@Test
	void givenAFullTeam_whenWithdrawingFromTheBox_thenAConflictProblemNamesTheTeam() {
		List<Map<String, Object>> captured = IntStream.range(0, 7).mapToObj(i -> capture(pikachu())).toList();

		transfer(captured.get(6).get("id"), "TEAM")
				.expectStatus().isEqualTo(HttpStatus.CONFLICT)
				.expectHeader().contentType("application/problem+json")
				.expectBody()
				.jsonPath("$.code").isEqualTo("storage-full")
				.jsonPath("$.area").isEqualTo("TEAM");
	}

	@Test
	void givenFullTeamAndBox_whenCapturing_thenAConflictProblemIsReturned() {
		IntStream.range(0, 8).forEach(i -> capture(pikachu()));

		postCapture(pikachu()).expectStatus().isEqualTo(HttpStatus.CONFLICT)
				.expectBody().jsonPath("$.code").isEqualTo("storage-full");
	}

	@Test
	void givenAMagikarpInTeamSlotTwo_whenEvolvingIntoGyarados_thenIdentityGeneticsAndSlotArePreservedAndTheAbilityChanges() {
		capture(pikachu());
		Map<String, Object> magikarp = capture(magikarp());

		http.post().uri("/api/v1/trainers/{t}/pokemon/{p}/evolution", trainer, magikarp.get("id"))
				.bodyValue(Map.of("targetSpecies", "gyarados"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(magikarp.get("id"))
				.jsonPath("$.species.name").isEqualTo("gyarados")
				.jsonPath("$.ability").isEqualTo("intimidate")
				.jsonPath("$.storage.slot").isEqualTo(2)
				.jsonPath("$.individualValues.speed").isEqualTo(31)
				.jsonPath("$.nature.name").isEqualTo("TIMID")
				.jsonPath("$.heldItem").isEqualTo("light-ball");
	}

	@Test
	void givenAnEevee_whenEvolvingIntoEachOfTwoBranches_thenBothAreValidDirectEvolutions() {
		Map<String, Object> first = capture(eevee());
		Map<String, Object> second = capture(eevee());

		evolve(first.get("id"), "vaporeon").expectStatus().isOk();
		evolve(second.get("id"), "jolteon").expectStatus().isOk()
				.expectBody().jsonPath("$.ability").isEqualTo("volt-absorb");
	}

	@Test
	void givenABulbasaur_whenSkippingAStageAndEvolvingIntoVenusaur_thenTheEvolutionIsRejected() {
		Map<String, Object> bulbasaur = capture(bulbasaur());

		evolve(bulbasaur.get("id"), "venusaur")
				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectBody().jsonPath("$.code").isEqualTo("evolution-not-allowed");
	}

	@Test
	void givenABoxedPokemon_whenEvolvingIt_thenAConflictProblemIsReturned() {
		Map<String, Object> eevee = capture(eevee());
		transfer(eevee.get("id"), "BOX").expectStatus().isOk();

		evolve(eevee.get("id"), "vaporeon")
				.expectStatus().isEqualTo(HttpStatus.CONFLICT)
				.expectBody().jsonPath("$.code").isEqualTo("pokemon-not-in-team");
	}

	@Test
	void givenEffortValuesAboveTheLimit_whenCapturing_thenABadRequestProblemExplainsTheRule() {
		Map<String, Object> request = pikachu();
		request.put("effortValues", stats(0, 253, 0, 0, 0, 0));

		postCapture(request).expectStatus().isBadRequest()
				.expectBody()
				.jsonPath("$.code").isEqualTo("invalid-value")
				.jsonPath("$.detail").value(detail -> assertThat(detail.toString()).contains("ATTACK", "252"));
	}

	@Test
	void givenAMissingMandatoryField_whenCapturing_thenABadRequestProblemIsReturned() {
		Map<String, Object> request = pikachu();
		request.remove("species");

		postCapture(request).expectStatus().isBadRequest()
				.expectHeader().contentType("application/problem+json");
	}

	@Test
	void givenAnAbilityAndMoveForeignToTheSpecies_whenCapturing_thenEveryViolationIsListed() {
		Map<String, Object> request = pikachu();
		request.put("ability", "intimidate");
		request.put("moves", List.of("hydro-pump"));

		postCapture(request).expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectBody()
				.jsonPath("$.code").isEqualTo("species-rule-violation")
				.jsonPath("$.violations.length()").isEqualTo(2);
	}

	@Test
	void givenASpeciesUnknownToPokeApi_whenCapturing_thenAnUnprocessableProblemIsReturned() {
		Map<String, Object> request = pikachu();
		request.put("species", "missingno");

		postCapture(request).expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectBody().jsonPath("$.code").isEqualTo("unknown-pokeapi-entry");
	}

	@Test
	void givenPokeApiDown_whenCapturing_thenAServiceUnavailableProblemIsReturned() {
		Map<String, Object> request = pikachu();
		request.put("species", "raichu");
		request.put("heldItem", null);
		POKE_API.failWith("pokemon-species/raichu", 503);

		postCapture(request).expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
				.expectBody().jsonPath("$.code").isEqualTo("pokeapi-unavailable");
	}

	@Test
	void givenASpeciesAlreadyLookedUp_whenCapturingItAgain_thenPokeApiIsServedFromTheCache() {
		Map<String, Object> magnemite = captureRequest("magnemite", "sturdy", List.of("tackle"), "GENDERLESS");

		capture(magnemite);
		capture(magnemite);

		assertThat(POKE_API.requestsTo("pokemon-species/magnemite")).isEqualTo(1);
		assertThat(POKE_API.requestsTo("pokemon/81")).isEqualTo(1);
	}

	@Test
	void givenAnUnknownTrainerOrPokemon_whenRequestingIt_thenNotFoundProblemsAreReturned() {
		http.get().uri("/api/v1/trainers/{t}/team", "00000000-0000-0000-0000-000000000000").exchange()
				.expectStatus().isNotFound().expectBody().jsonPath("$.code").isEqualTo("not-found");
		http.get().uri("/api/v1/trainers/{t}/pokemon/{p}", trainer, "00000000-0000-0000-0000-000000000000").exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void givenRegisteredTrainers_whenListingThemInPagesOfOne_thenEachPageHoldsOneTrainerAndTheTotalCountsAll() {
		http.post().uri("/api/v1/trainers").bodyValue(Map.of("name", "Misty")).exchange().expectStatus().isCreated();

		http.get().uri("/api/v1/trainers?page=0&size=1").exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.trainers.length()").isEqualTo(1)
				.jsonPath("$.trainers[0].id").isNotEmpty()
				.jsonPath("$.size").isEqualTo(1)
				.jsonPath("$.totalElements").value(total -> assertThat(((Number) total).longValue()).isGreaterThanOrEqualTo(2))
				.jsonPath("$.totalPages").value(pages -> assertThat(((Number) pages).longValue()).isGreaterThanOrEqualTo(2));
	}

	@Test
	void givenACapturedPokemon_whenListingAllPokemon_thenTheLastPageEndsWithItAndCarriesItsOwnerAndStorage() {
		Map<String, Object> pikachu = capture(pikachu());
		long total = ((Number) http.get().uri("/api/v1/pokemon?size=1").exchange()
				.expectStatus().isOk()
				.expectBody(Map.class).returnResult().getResponseBody().get("totalElements")).longValue();

		http.get().uri("/api/v1/pokemon?page={page}&size=1", total - 1).exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.pokemon[0].id").isEqualTo(pikachu.get("id"))
				.jsonPath("$.pokemon[0].trainerId").isEqualTo(trainer)
				.jsonPath("$.pokemon[0].storage.area").isEqualTo("TEAM");
	}

	@Test
	void givenAPageSizeAboveTheLimit_whenListingTrainersOrPokemon_thenABadRequestProblemIsReturned() {
		http.get().uri("/api/v1/trainers?size=101").exchange()
				.expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo("invalid-value");
		http.get().uri("/api/v1/pokemon?page=-1").exchange()
				.expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo("invalid-value");
	}

	@Test
	void givenTwentyConcurrentCaptures_whenTheyRace_thenStorageNeverExceedsItsCapacity() {
		List<HttpStatusCode> results = Flux.range(0, 20)
				.parallel(20)
				.runOn(Schedulers.boundedElastic())
				.map(i -> http.post().uri("/api/v1/trainers/{t}/pokemon", trainer).bodyValue(pikachu())
						.exchange().returnResult(String.class).getStatus())
				.sequential()
				.collectList()
				.block();

		assertThat(results).allMatch(status -> status.value() == 201 || status.value() == 409);
		http.get().uri("/api/v1/trainers/{t}/team", trainer).exchange()
				.expectBody().jsonPath("$.members.length()").isEqualTo(6);
		http.get().uri("/api/v1/trainers/{t}/box", trainer).exchange()
				.expectBody().jsonPath("$.totalElements").isEqualTo(2);
	}

	@Test
	void givenAnEmptyTrainer_whenAsManyCapturesAsFreeSlotsRaceAtOnce_thenEveryOneIsStored() {
		List<Integer> statuses = concurrently(8, i -> postCapture(pikachu()).returnResult(String.class).getStatus().value());

		assertThat(statuses).containsOnly(201);
		http.get().uri("/api/v1/trainers/{t}/box", trainer).exchange()
				.expectBody().jsonPath("$.totalElements").isEqualTo(2);
	}

	@RepeatedTest(5)
	void givenAnEvolutionAndADepositOfTheSamePokemonAtOnce_whenBothFinish_thenNoAcceptedChangeIsLost() {
		Object magikarp = capture(magikarp()).get("id");

		List<Map<String, Object>> results = concurrently(2, i -> {
			WebTestClient.ResponseSpec response = i == 0 ? evolve(magikarp, "gyarados") : transfer(magikarp, "BOX");
			return response.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
			}).returnResult().getResponseBody();
		});

		Map<String, Object> stored = http.get().uri("/api/v1/trainers/{t}/pokemon/{p}", trainer, magikarp).exchange()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();
		Map<String, Object> evolution = results.get(0);
		Map<String, Object> deposit = results.get(1);
		assertThat(storage(deposit).get("area")).as("the deposit always succeeds").isEqualTo("BOX");
		assertThat(storage(stored).get("area")).isEqualTo("BOX");
		if (evolution.containsKey("species")) {
			assertThat(species(stored)).as("an accepted evolution is kept").isEqualTo("gyarados");
		} else {
			assertThat(evolution.get("code")).isEqualTo("pokemon-not-in-team");
			assertThat(species(stored)).isEqualTo("magikarp");
		}
	}

	@Test
	void givenAnInvalidEnumAndABlankField_whenCapturing_thenAMalformedRequestProblemNamesBothFields() {
		Map<String, Object> request = pikachu();
		request.put("nature", "BRAVEST");

		postCapture(request).expectStatus().isBadRequest()
				.expectHeader().contentType("application/problem+json")
				.expectBody()
				.jsonPath("$.code").isEqualTo("malformed-request")
				.jsonPath("$.type").isEqualTo("urn:pokestorage:problem:malformed-request")
				.jsonPath("$.violations[0]").value(violation -> assertThat(violation.toString())
						.startsWith("nature: invalid value 'BRAVEST'").contains("TIMID"));
		request.put("nature", "TIMID");
		request.put("species", " ");
		postCapture(request).expectStatus().isBadRequest()
				.expectBody()
				.jsonPath("$.code").isEqualTo("malformed-request")
				.jsonPath("$.violations[0]").isEqualTo("species: must not be blank");
	}

	@Test
	void givenAMalformedPathOrQueryOrBody_whenCallingTheApi_thenEveryBadRequestCarriesACodeAndTheCulprit() {
		http.get().uri("/api/v1/trainers/not-a-uuid").exchange()
				.expectStatus().isBadRequest().expectBody()
				.jsonPath("$.code").isEqualTo("malformed-request")
				.jsonPath("$.violations[0]").value(violation -> assertThat(violation.toString()).startsWith("trainerId:"));
		http.get().uri("/api/v1/trainers?page=abc").exchange()
				.expectStatus().isBadRequest().expectBody()
				.jsonPath("$.violations[0]").value(violation -> assertThat(violation.toString()).startsWith("page:"));
		http.post().uri("/api/v1/trainers").header("Content-Type", "application/json").bodyValue("{\"name\":").exchange()
				.expectStatus().isBadRequest().expectBody()
				.jsonPath("$.code").isEqualTo("malformed-request")
				.jsonPath("$.violations[0]").isEqualTo("body: malformed JSON");
		http.delete().uri("/api/v1/trainers").exchange()
				.expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED).expectBody()
				.jsonPath("$.code").isEqualTo("method-not-allowed");
	}

	/**
	 * Sends the requests at once and returns their results in request order.
	 */
	private <T> List<T> concurrently(int requests, IntFunction<T> request) {
		return Flux.range(0, requests)
				.parallel(requests)
				.runOn(Schedulers.boundedElastic())
				.map(i -> Map.entry(i, request.apply(i)))
				.sequential()
				.sort(Map.Entry.comparingByKey())
				.map(Map.Entry::getValue)
				.collectList()
				.block();
	}

	@SuppressWarnings("unchecked")
	private static String species(Map<String, Object> pokemon) {
		return ((Map<String, Object>) pokemon.get("species")).get("name").toString();
	}

	private Map<String, Object> capture(Map<String, Object> request) {
		return postCapture(request).expectStatus().isCreated()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.returnResult().getResponseBody();
	}

	private WebTestClient.ResponseSpec postCapture(Map<String, Object> request) {
		return http.post().uri("/api/v1/trainers/{t}/pokemon", trainer).bodyValue(request).exchange();
	}

	private WebTestClient.ResponseSpec transfer(Object pokemon, String area) {
		return http.put().uri("/api/v1/trainers/{t}/pokemon/{p}/storage", trainer, pokemon)
				.bodyValue(Map.of("area", area)).exchange();
	}

	private WebTestClient.ResponseSpec evolve(Object pokemon, String target) {
		return http.post().uri("/api/v1/trainers/{t}/pokemon/{p}/evolution", trainer, pokemon)
				.bodyValue(Map.of("targetSpecies", target)).exchange();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> storage(Map<String, Object> pokemon) {
		return (Map<String, Object>) pokemon.get("storage");
	}

	private static Map<String, Object> pikachu() {
		return captureRequest("pikachu", "static", List.of("thunderbolt", "quick-attack"), "FEMALE");
	}

	private static Map<String, Object> magikarp() {
		return captureRequest("magikarp", "swift-swim", List.of("splash"), "MALE");
	}

	private static Map<String, Object> eevee() {
		return captureRequest("eevee", "run-away", List.of("tackle"), "MALE");
	}

	private static Map<String, Object> bulbasaur() {
		return captureRequest("bulbasaur", "overgrow", List.of("tackle"), "MALE");
	}

	private static Map<String, Object> captureRequest(String species, String ability, List<String> moves, String gender) {
		Map<String, Object> request = new HashMap<>();
		request.put("species", species);
		request.put("nickname", "Sparky");
		request.put("level", 25);
		request.put("individualValues", stats(31, 31, 31, 31, 31, 31));
		request.put("effortValues", stats(4, 0, 0, 252, 0, 252));
		request.put("nature", "TIMID");
		request.put("ability", ability);
		request.put("gender", gender);
		request.put("shiny", true);
		request.put("moves", moves);
		request.put("heldItem", "light-ball");
		request.put("origin", Map.of("pokeball", "poke-ball", "location", "viridian-forest"));
		return request;
	}

	private static Map<String, Object> stats(int hp, int attack, int defense, int specialAttack, int specialDefense,
			int speed) {
		return Map.of("hp", hp, "attack", attack, "defense", defense, "specialAttack", specialAttack,
				"specialDefense", specialDefense, "speed", speed);
	}

}
