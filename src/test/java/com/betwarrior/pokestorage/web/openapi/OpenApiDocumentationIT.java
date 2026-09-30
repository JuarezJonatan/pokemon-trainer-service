package com.betwarrior.pokestorage.web.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.betwarrior.pokestorage.testsupport.PostgresContainerConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgresContainerConfiguration.class)
class OpenApiDocumentationIT {

	private static final String CAPTURE = "$.paths['/api/v1/trainers/{trainerId}/pokemon'].post";
	private static final String EVOLUTION = "$.paths['/api/v1/trainers/{trainerId}/pokemon/{pokemonId}/evolution'].post";

	@Autowired
	private WebTestClient http;

	@Test
	void givenTheRunningService_whenRequestingTheApiDocs_thenEveryEndpointIsDocumentedUnderItsTag() {
		WebTestClient.BodyContentSpec docs = apiDocs();

		docs.jsonPath("$.info.title").isEqualTo("Pokemon Trainer Service API")
				.jsonPath("$.tags[*].name").value(tags -> assertThat(tags.toString())
						.contains("Trainers", "Pokemon", "Team & Box"))
				.jsonPath("$.paths['/api/v1/trainers'].post.summary").isEqualTo("Register a trainer")
				.jsonPath("$.paths['/api/v1/trainers/{trainerId}'].get.summary").isEqualTo("Get a trainer")
				.jsonPath(CAPTURE + ".summary").isEqualTo("Capture a Pokemon")
				.jsonPath("$.paths['/api/v1/trainers/{trainerId}/pokemon/{pokemonId}'].get.summary").isEqualTo("Get a Pokemon")
				.jsonPath("$.paths['/api/v1/trainers/{trainerId}/team'].get.tags[0]").isEqualTo("Team & Box")
				.jsonPath("$.paths['/api/v1/trainers/{trainerId}/box'].get.tags[0]").isEqualTo("Team & Box")
				.jsonPath("$.paths['/api/v1/trainers/{trainerId}/pokemon/{pokemonId}/storage'].put.tags[0]").isEqualTo("Team & Box")
				.jsonPath(EVOLUTION + ".summary").isEqualTo("Evolve a team member");
	}

	@Test
	void givenTheCaptureEndpoint_whenRequestingTheApiDocs_thenItsRequestExampleAndErrorResponsesAreDescribed() {
		WebTestClient.BodyContentSpec docs = apiDocs();

		docs.jsonPath(CAPTURE + ".requestBody.content['application/json'].examples.pikachu.value").exists()
				.jsonPath(CAPTURE + ".responses['201'].headers.Location").exists()
				.jsonPath(CAPTURE + ".responses['400']['$ref']").isEqualTo("#/components/responses/BadRequest")
				.jsonPath(CAPTURE + ".responses['409']['$ref']").isEqualTo("#/components/responses/CaptureConflict")
				.jsonPath(CAPTURE + ".responses['422']['$ref']").isEqualTo("#/components/responses/CaptureRejected")
				.jsonPath(CAPTURE + ".responses['503']['$ref']").isEqualTo("#/components/responses/PokeApiUnavailable");
	}

	@Test
	void givenTheErrorModel_whenRequestingTheApiDocs_thenProblemResponsesShowAnExamplePerCode() {
		WebTestClient.BodyContentSpec docs = apiDocs();

		docs.jsonPath("$.components.schemas.Problem.properties.code").exists()
				.jsonPath("$.components.schemas.Problem.properties.violations").exists()
				.jsonPath("$.components.responses.CaptureRejected.content['application/problem+json'].examples['species-rule-violation'].value.violations.length()")
				.isEqualTo(2)
				.jsonPath("$.components.responses.EvolutionRejected.content['application/problem+json'].examples['evolution-not-allowed'].value.code")
				.isEqualTo("evolution-not-allowed")
				.jsonPath("$.components.responses.TransferConflict.content['application/problem+json'].examples['storage-full'].value.area")
				.isEqualTo("TEAM");
	}

	@Test
	void givenTheCaptureRequest_whenRequestingTheApiDocs_thenItsFieldsCarryDescriptionsAndLimits() {
		WebTestClient.BodyContentSpec docs = apiDocs();

		docs.jsonPath("$.components.schemas.CaptureRequest.properties.level.minimum").isEqualTo(1)
				.jsonPath("$.components.schemas.CaptureRequest.properties.level.maximum").isEqualTo(100)
				.jsonPath("$.components.schemas.CaptureRequest.properties.moves.maxItems").isEqualTo(4)
				.jsonPath("$.components.schemas.CaptureRequest.properties.effortValues.description").value(description ->
						assertThat(description.toString()).contains("510"))
				.jsonPath("$.components.schemas.CaptureRequest.required").value(required ->
						assertThat(required.toString()).contains("species", "individualValues"));
	}

	private WebTestClient.BodyContentSpec apiDocs() {
		return http.get().uri("/v3/api-docs").exchange()
				.expectStatus().isOk()
				.expectBody();
	}

}
