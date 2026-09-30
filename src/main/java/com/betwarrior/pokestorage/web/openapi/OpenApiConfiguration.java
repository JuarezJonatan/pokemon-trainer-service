package com.betwarrior.pokestorage.web.openapi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * API metadata and the reusable error responses referenced from the controllers through
 * {@code @ApiResponse(ref = ...)}. Each response lists an example per {@code code} it can carry.
 */
@Configuration
public class OpenApiConfiguration {

	public static final String TRAINERS_TAG = "Trainers";
	public static final String POKEMON_TAG = "Pokemon";
	public static final String STORAGE_TAG = "Team & Box";

	public static final String BAD_REQUEST = "BadRequest";
	public static final String NOT_FOUND = "NotFound";
	public static final String CAPTURE_CONFLICT = "CaptureConflict";
	public static final String TRANSFER_CONFLICT = "TransferConflict";
	public static final String EVOLUTION_CONFLICT = "EvolutionConflict";
	public static final String CAPTURE_REJECTED = "CaptureRejected";
	public static final String EVOLUTION_REJECTED = "EvolutionRejected";
	public static final String POKEAPI_UNAVAILABLE = "PokeApiUnavailable";

	private static final String TRAINER = "/api/v1/trainers/5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60";
	private static final String POKEMON = TRAINER + "/pokemon/0a9e3d8c-7f41-4b62-a6d3-2c1b0e9f8d7a";

	@Bean
	public OpenAPI pokemonStorageOpenApi() {
		Components components = new Components();
		ModelConverters.getInstance().readAll(ApiProblem.class).forEach(components::addSchemas);
		problemResponses().forEach(components::addResponses);
		return new OpenAPI()
				.info(new Info()
						.title("Pokemon Trainer Service API")
						.version("v1")
						.description("""
								Manages each trainer's individual Pokemon, split between the **active team** \
								and the **PC box**.

								- Species, abilities, moves and items are validated against [PokeAPI](https://pokeapi.co) \
								and use its names (lowercase, hyphenated). Input is normalized: `Pikachu` equals `pikachu`.
								- New captures go to the first free team slot, then to the first free box slot. \
								Slots are never compacted: a Pokemon keeps its exact position.
								- Errors follow [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) \
								(`application/problem+json`) and carry a stable `code`.
								""")
						.license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
				.tags(List.of(
						new Tag().name(TRAINERS_TAG).description("Trainer registration and lookup"),
						new Tag().name(POKEMON_TAG).description("Capture, detail and evolution of individual Pokemon"),
						new Tag().name(STORAGE_TAG).description("Active team, PC box and transfers between them")))
				.components(components);
	}

	private static Map<String, ApiResponse> problemResponses() {
		Map<String, ApiResponse> responses = new LinkedHashMap<>();
		responses.put(BAD_REQUEST, problem("The request is malformed or breaks a value invariant",
				Map.entry("invalid-value", problemExample(400, "Bad Request", "invalid-value",
						"EVs must add up to at most 510 but were 512", TRAINER + "/pokemon")),
				Map.entry("malformed-request", example("Malformed JSON, missing required field or invalid enum/UUID",
						frameworkBadRequest(TRAINER + "/pokemon")))));
		responses.put(NOT_FOUND, problem("The trainer does not exist, or the Pokemon does not belong to the trainer",
				Map.entry("trainer", problemExample(404, "Not Found", "not-found",
						"Trainer 5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60 does not exist", TRAINER)),
				Map.entry("pokemon", problemExample(404, "Not Found", "not-found",
						"Trainer 5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60 has no Pokemon 0a9e3d8c-7f41-4b62-a6d3-2c1b0e9f8d7a",
						POKEMON))));
		responses.put(CAPTURE_CONFLICT, problem("There is no room left, or the slot was taken concurrently",
				Map.entry("storage-full", problemExample(409, "Conflict", "storage-full",
						"Both the active team and the PC box are full", TRAINER + "/pokemon")),
				Map.entry("concurrent-modification", concurrentModification(TRAINER + "/pokemon"))));
		responses.put(TRANSFER_CONFLICT, problem("The destination area is full, or the slot was taken concurrently",
				Map.entry("storage-full", example("The destination area is full", withProperty(
						problemBody(409, "Conflict", "storage-full", "The active team is full (capacity 6)",
								POKEMON + "/storage"), "area", "TEAM"))),
				Map.entry("concurrent-modification", concurrentModification(POKEMON + "/storage"))));
		responses.put(EVOLUTION_CONFLICT, problem("Only team members can evolve",
				Map.entry("pokemon-not-in-team", problemExample(409, "Conflict", "pokemon-not-in-team",
						"Pokemon 0a9e3d8c-7f41-4b62-a6d3-2c1b0e9f8d7a must be in the active team to evolve",
						POKEMON + "/evolution"))));
		responses.put(CAPTURE_REJECTED, problem("The Pokemon is not consistent with PokeAPI data",
				Map.entry("species-rule-violation", speciesRuleViolation(TRAINER + "/pokemon", List.of(
						"pikachu cannot have ability 'intimidate' (allowed: [static, lightning-rod])",
						"pikachu cannot learn move 'hydro-pump'"))),
				Map.entry("unknown-pokeapi-entry", problemExample(422, "Unprocessable Entity", "unknown-pokeapi-entry",
						"Unknown species 'pikachuu' in PokeAPI", TRAINER + "/pokemon")),
				Map.entry("invalid-item", problemExample(422, "Unprocessable Entity", "invalid-item",
						"'potion' is not a pokeball", TRAINER + "/pokemon"))));
		responses.put(EVOLUTION_REJECTED, problem("The evolution is not allowed by PokeAPI data",
				Map.entry("evolution-not-allowed", problemExample(422, "Unprocessable Entity", "evolution-not-allowed",
						"venusaur is not a direct evolution of bulbasaur", POKEMON + "/evolution")),
				Map.entry("species-rule-violation", speciesRuleViolation(POKEMON + "/evolution", List.of(
						"vaporeon cannot have ability 'volt-absorb' (allowed: [water-absorb, hydration])"))),
				Map.entry("unknown-pokeapi-entry", problemExample(422, "Unprocessable Entity", "unknown-pokeapi-entry",
						"Unknown species 'vaporeonn' in PokeAPI", POKEMON + "/evolution"))));
		responses.put(POKEAPI_UNAVAILABLE, problem("PokeAPI did not answer, even after retrying",
				Map.entry("pokeapi-unavailable", problemExample(503, "Service Unavailable", "pokeapi-unavailable",
						"PokeAPI is not available right now, please retry later", TRAINER + "/team"))));
		return responses;
	}

	@SafeVarargs
	private static ApiResponse problem(String description, Map.Entry<String, Example>... examples) {
		Map<String, Example> ordered = new LinkedHashMap<>();
		for (Map.Entry<String, Example> entry : examples) {
			ordered.put(entry.getKey(), entry.getValue());
		}
		var mediaType = new io.swagger.v3.oas.models.media.MediaType()
				.schema(new Schema<>().$ref("#/components/schemas/Problem"))
				.examples(ordered);
		return new ApiResponse().description(description)
				.content(new Content().addMediaType(MediaType.APPLICATION_PROBLEM_JSON_VALUE, mediaType));
	}

	private static Example concurrentModification(String instance) {
		return problemExample(409, "Conflict", "concurrent-modification",
				"The storage changed concurrently, please retry", instance);
	}

	private static Example speciesRuleViolation(String instance, List<String> violations) {
		return example("Every broken rule is listed in `violations`", withProperty(
				problemBody(422, "Unprocessable Entity", "species-rule-violation",
						"The Pokemon does not comply with its species data in PokeAPI", instance),
				"violations", violations));
	}

	private static Example problemExample(int status, String title, String code, String detail, String instance) {
		return example(detail, problemBody(status, title, code, detail, instance));
	}

	private static Map<String, Object> problemBody(int status, String title, String code, String detail,
			String instance) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "urn:pokestorage:problem:" + code);
		body.put("title", title);
		body.put("status", status);
		body.put("detail", detail);
		body.put("instance", instance);
		body.put("code", code);
		return body;
	}

	private static Map<String, Object> frameworkBadRequest(String instance) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "about:blank");
		body.put("title", "Bad Request");
		body.put("status", 400);
		body.put("detail", "Invalid request content.");
		body.put("instance", instance);
		return body;
	}

	private static Map<String, Object> withProperty(Map<String, Object> body, String name, Object value) {
		body.put(name, value);
		return body;
	}

	private static Example example(String summary, Map<String, Object> value) {
		return new Example().summary(summary).value(value);
	}

}
