package com.betwarrior.pokestorage.web.openapi;

import java.net.URI;
import java.util.List;

import com.betwarrior.pokestorage.domain.storage.StorageArea;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OpenAPI description of the RFC 7807 problem details returned by {@link com.betwarrior.pokestorage.web.error.ProblemHandler}. It only
 * documents the contract; responses are built as {@link org.springframework.http.ProblemDetail}.
 */
@Schema(name = "Problem", description = "RFC 7807 problem details. Clients should branch on `code`, never on `detail`.")
record ApiProblem(
		@Schema(description = "URI identifying the problem type", example = "urn:pokestorage:problem:not-found")
		URI type,
		@Schema(description = "Short summary of the HTTP status", example = "Not Found")
		String title,
		@Schema(description = "HTTP status code", example = "404")
		int status,
		@Schema(description = "Human readable explanation, meant for logs and developers",
				example = "Trainer 5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60 does not exist")
		String detail,
		@Schema(description = "Request path that produced the problem",
				example = "/api/v1/trainers/5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60")
		URI instance,
		@Schema(description = """
				Stable machine readable error code. Other framework-level errors use the HTTP status name, \
				e.g. `method-not-allowed` or `unsupported-media-type`""",
				allowableValues = { "malformed-request", "invalid-value", "not-found", "storage-full", "pokemon-not-in-team",
						"concurrent-modification", "species-rule-violation", "unknown-pokeapi-entry", "invalid-item",
						"evolution-not-allowed", "pokeapi-unavailable" },
				example = "not-found")
		String code,
		@Schema(description = "Only for `storage-full`: the area that is full. Absent when both team and box are full")
		StorageArea area,
		@Schema(description = "Only for `species-rule-violation` and `malformed-request`: every rule or field that is wrong")
		List<String> violations) {
}
