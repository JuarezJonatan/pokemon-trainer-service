package com.betwarrior.pokestorage.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.betwarrior.pokestorage.application.usecase.pokemon.ListAllPokemon;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.PokemonPageResponse;
import com.betwarrior.pokestorage.web.openapi.OpenApiConfiguration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Pokemon across all trainers. Per-trainer operations live in {@link PokemonController}.
 */
@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = OpenApiConfiguration.POKEMON_TAG)
@RequiredArgsConstructor
public class AllPokemonController {

	private final ListAllPokemon listAllPokemon;

	@Operation(summary = "List all Pokemon",
			description = """
					Paginated list of every stored Pokemon, of all trainers and in both team and box, in the order \
					they were stored. Each one carries its `trainerId` and `storage`. It does not call PokeAPI.""")
	@ApiResponse(responseCode = "200", description = "A page of Pokemon")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@GetMapping
	public Mono<PokemonPageResponse> list(
			@Parameter(description = "Zero-based page number", schema = @Schema(minimum = "0", defaultValue = "0"))
			@RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", schema = @Schema(minimum = "1", maximum = "100", defaultValue = "20"))
			@RequestParam(defaultValue = "20") int size) {
		return listAllPokemon.list(page, size).map(PokemonPageResponse::from);
	}

}
