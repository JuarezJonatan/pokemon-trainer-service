package com.betwarrior.pokestorage.web.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.betwarrior.pokestorage.application.config.StorageProperties;
import com.betwarrior.pokestorage.application.usecase.pokemon.CapturePokemon;
import com.betwarrior.pokestorage.application.usecase.pokemon.EvolvePokemon;
import com.betwarrior.pokestorage.application.usecase.pokemon.FindPokemon;
import com.betwarrior.pokestorage.application.usecase.storage.ListBox;
import com.betwarrior.pokestorage.application.usecase.storage.ListTeam;
import com.betwarrior.pokestorage.application.usecase.storage.TransferPokemon;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.BoxResponse;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.CaptureRequest;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.EvolutionRequest;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.PokemonResponse;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.StorageRequest;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.TeamMemberResponse;
import com.betwarrior.pokestorage.web.dto.PokemonPayloads.TeamResponse;
import com.betwarrior.pokestorage.web.openapi.OpenApiConfiguration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/trainers/{trainerId}")
@RequiredArgsConstructor
public class PokemonController {

	private static final String TRAINER_ID_EXAMPLE = "5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60";
	private static final String POKEMON_ID_EXAMPLE = "0a9e3d8c-7f41-4b62-a6d3-2c1b0e9f8d7a";
	private static final String CAPTURE_EXAMPLE = """
			{
			  "species": "pikachu",
			  "nickname": "Sparky",
			  "level": 25,
			  "individualValues": {"hp": 31, "attack": 31, "defense": 31, "specialAttack": 31, "specialDefense": 31, "speed": 31},
			  "effortValues": {"hp": 4, "attack": 0, "defense": 0, "specialAttack": 252, "specialDefense": 0, "speed": 252},
			  "nature": "TIMID",
			  "ability": "static",
			  "gender": "FEMALE",
			  "shiny": true,
			  "moves": ["thunderbolt", "quick-attack"],
			  "heldItem": "light-ball",
			  "origin": {"pokeball": "poke-ball", "location": "viridian-forest"}
			}
			""";

	private final CapturePokemon capturePokemon;
	private final FindPokemon findPokemon;
	private final ListTeam listTeam;
	private final ListBox listBox;
	private final TransferPokemon transferPokemon;
	private final EvolvePokemon evolvePokemon;
	private final StorageProperties storage;

	@Tag(name = OpenApiConfiguration.POKEMON_TAG)
	@Operation(summary = "Capture a Pokemon",
			description = """
					Creates an individual Pokemon for the trainer. Value invariants (IVs, EVs, level, moves) are \
					checked first; then species, ability, moves, gender, Poke Ball and held item are validated \
					against PokeAPI. The Pokemon is placed in the first free team slot or, if the team is full, \
					in the first free box slot. `storage` in the response tells where it ended up.""",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(implementation = CaptureRequest.class),
							examples = @ExampleObject(name = "pikachu", value = CAPTURE_EXAMPLE))))
	@ApiResponse(responseCode = "201", description = "Pokemon captured",
			headers = @Header(name = "Location", description = "URL of the new Pokemon",
					schema = @Schema(type = "string", format = "uri")))
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@ApiResponse(responseCode = "409", ref = OpenApiConfiguration.CAPTURE_CONFLICT)
	@ApiResponse(responseCode = "422", ref = OpenApiConfiguration.CAPTURE_REJECTED)
	@ApiResponse(responseCode = "503", ref = OpenApiConfiguration.POKEAPI_UNAVAILABLE)
	@PostMapping("/pokemon")
	public Mono<ResponseEntity<PokemonResponse>> capture(
			@Parameter(description = "Trainer id", example = TRAINER_ID_EXAMPLE) @PathVariable UUID trainerId,
			@Valid @RequestBody CaptureRequest request, UriComponentsBuilder uri) {
		return capturePokemon.capture(request.toCommand(new TrainerId(trainerId)))
				.map(pokemon -> ResponseEntity
						.created(uri.path("/api/v1/trainers/{trainerId}/pokemon/{pokemonId}")
								.build(trainerId, pokemon.id().value()))
						.body(PokemonResponse.from(pokemon)));
	}

	@Tag(name = OpenApiConfiguration.POKEMON_TAG)
	@Operation(summary = "Get a Pokemon",
			description = "Full detail of one of the trainer's Pokemon: genetics, training, origin and storage position.")
	@ApiResponse(responseCode = "200", description = "The Pokemon")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@GetMapping("/pokemon/{pokemonId}")
	public Mono<PokemonResponse> find(
			@Parameter(description = "Trainer id", example = TRAINER_ID_EXAMPLE) @PathVariable UUID trainerId,
			@Parameter(description = "Pokemon id", example = POKEMON_ID_EXAMPLE) @PathVariable UUID pokemonId) {
		return findPokemon.find(new TrainerId(trainerId), new PokemonId(pokemonId)).map(PokemonResponse::from);
	}

	@Tag(name = OpenApiConfiguration.STORAGE_TAG)
	@Operation(summary = "List the active team",
			description = """
					Composite view ordered by slot: each member combines the stored Pokemon with its species \
					data from PokeAPI (types, base stats, sprite) and its stats calculated with the official \
					formula. PokeAPI responses are cached.""")
	@ApiResponse(responseCode = "200", description = "The active team")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@ApiResponse(responseCode = "503", ref = OpenApiConfiguration.POKEAPI_UNAVAILABLE)
	@GetMapping("/team")
	public Mono<TeamResponse> team(
			@Parameter(description = "Trainer id", example = TRAINER_ID_EXAMPLE) @PathVariable UUID trainerId) {
		return listTeam.list(new TrainerId(trainerId))
				.map(members -> new TeamResponse(storage.teamCapacity(),
						members.stream().map(TeamMemberResponse::from).toList()));
	}

	@Tag(name = OpenApiConfiguration.STORAGE_TAG)
	@Operation(summary = "List the PC box",
			description = "Paginated list of the Pokemon in the box, ordered by slot. It does not call PokeAPI.")
	@ApiResponse(responseCode = "200", description = "A page of the box")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@GetMapping("/box")
	public Mono<BoxResponse> box(
			@Parameter(description = "Trainer id", example = TRAINER_ID_EXAMPLE) @PathVariable UUID trainerId,
			@Parameter(description = "Zero-based page number", schema = @Schema(minimum = "0", defaultValue = "0"))
			@RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", schema = @Schema(minimum = "1", maximum = "100", defaultValue = "30"))
			@RequestParam(defaultValue = "30") int size) {
		return listBox.list(new TrainerId(trainerId), page, size).map(BoxResponse::from);
	}

	@Tag(name = OpenApiConfiguration.STORAGE_TAG)
	@Operation(summary = "Move a Pokemon between team and box",
			description = """
					Deposits a team member into the box or withdraws a boxed Pokemon into the team, taking the \
					first free slot of the destination. Idempotent: moving a Pokemon to the area it is already \
					in leaves it untouched.""")
	@ApiResponse(responseCode = "200", description = "The Pokemon in its new position")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@ApiResponse(responseCode = "409", ref = OpenApiConfiguration.TRANSFER_CONFLICT)
	@PutMapping("/pokemon/{pokemonId}/storage")
	public Mono<PokemonResponse> transfer(
			@Parameter(description = "Trainer id", example = TRAINER_ID_EXAMPLE) @PathVariable UUID trainerId,
			@Parameter(description = "Pokemon id", example = POKEMON_ID_EXAMPLE) @PathVariable UUID pokemonId,
			@Valid @RequestBody StorageRequest request) {
		return transferPokemon.transfer(new TrainerId(trainerId), new PokemonId(pokemonId), request.area())
				.map(PokemonResponse::from);
	}

	@Tag(name = OpenApiConfiguration.POKEMON_TAG)
	@Operation(summary = "Evolve a team member",
			description = """
					Evolves the Pokemon into a **direct** evolution of its current species (branched lines such \
					as Eevee are supported; skipping stages is not). Situational requirements (level, items, \
					friendship) are not checked. The ability is the requested one if given; otherwise the one \
					in the same ability slot, falling back to the first non-hidden ability. Identity, genetics, \
					training, held item, origin and exact team slot are preserved.""")
	@ApiResponse(responseCode = "200", description = "The evolved Pokemon")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@ApiResponse(responseCode = "409", ref = OpenApiConfiguration.EVOLUTION_CONFLICT)
	@ApiResponse(responseCode = "422", ref = OpenApiConfiguration.EVOLUTION_REJECTED)
	@ApiResponse(responseCode = "503", ref = OpenApiConfiguration.POKEAPI_UNAVAILABLE)
	@PostMapping("/pokemon/{pokemonId}/evolution")
	public Mono<PokemonResponse> evolve(
			@Parameter(description = "Trainer id", example = TRAINER_ID_EXAMPLE) @PathVariable UUID trainerId,
			@Parameter(description = "Pokemon id", example = POKEMON_ID_EXAMPLE) @PathVariable UUID pokemonId,
			@Valid @RequestBody EvolutionRequest request) {
		return evolvePokemon.evolve(new TrainerId(trainerId), new PokemonId(pokemonId), request.targetSpecies(),
				Optional.ofNullable(request.ability()))
				.map(PokemonResponse::from);
	}

}
