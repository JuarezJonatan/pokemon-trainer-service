package com.betwarrior.pokestorage.web;

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

import com.betwarrior.pokestorage.application.CapturePokemon;
import com.betwarrior.pokestorage.application.EvolvePokemon;
import com.betwarrior.pokestorage.application.FindPokemon;
import com.betwarrior.pokestorage.application.ListBox;
import com.betwarrior.pokestorage.application.ListTeam;
import com.betwarrior.pokestorage.application.StorageProperties;
import com.betwarrior.pokestorage.application.TransferPokemon;
import com.betwarrior.pokestorage.domain.PokemonId;
import com.betwarrior.pokestorage.domain.TrainerId;
import com.betwarrior.pokestorage.web.PokemonPayloads.BoxResponse;
import com.betwarrior.pokestorage.web.PokemonPayloads.CaptureRequest;
import com.betwarrior.pokestorage.web.PokemonPayloads.EvolutionRequest;
import com.betwarrior.pokestorage.web.PokemonPayloads.PokemonResponse;
import com.betwarrior.pokestorage.web.PokemonPayloads.StorageRequest;
import com.betwarrior.pokestorage.web.PokemonPayloads.TeamMemberResponse;
import com.betwarrior.pokestorage.web.PokemonPayloads.TeamResponse;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/trainers/{trainerId}")
public class PokemonController {

	private final CapturePokemon capturePokemon;
	private final FindPokemon findPokemon;
	private final ListTeam listTeam;
	private final ListBox listBox;
	private final TransferPokemon transferPokemon;
	private final EvolvePokemon evolvePokemon;
	private final StorageProperties storage;

	public PokemonController(CapturePokemon capturePokemon, FindPokemon findPokemon, ListTeam listTeam,
			ListBox listBox, TransferPokemon transferPokemon, EvolvePokemon evolvePokemon, StorageProperties storage) {
		this.capturePokemon = capturePokemon;
		this.findPokemon = findPokemon;
		this.listTeam = listTeam;
		this.listBox = listBox;
		this.transferPokemon = transferPokemon;
		this.evolvePokemon = evolvePokemon;
		this.storage = storage;
	}

	@PostMapping("/pokemon")
	public Mono<ResponseEntity<PokemonResponse>> capture(@PathVariable UUID trainerId,
			@Valid @RequestBody CaptureRequest request, UriComponentsBuilder uri) {
		return capturePokemon.capture(request.toCommand(new TrainerId(trainerId)))
				.map(pokemon -> ResponseEntity
						.created(uri.path("/api/v1/trainers/{trainerId}/pokemon/{pokemonId}")
								.build(trainerId, pokemon.id().value()))
						.body(PokemonResponse.from(pokemon)));
	}

	@GetMapping("/pokemon/{pokemonId}")
	public Mono<PokemonResponse> find(@PathVariable UUID trainerId, @PathVariable UUID pokemonId) {
		return findPokemon.find(new TrainerId(trainerId), new PokemonId(pokemonId)).map(PokemonResponse::from);
	}

	@GetMapping("/team")
	public Mono<TeamResponse> team(@PathVariable UUID trainerId) {
		return listTeam.list(new TrainerId(trainerId))
				.map(members -> new TeamResponse(storage.teamCapacity(),
						members.stream().map(TeamMemberResponse::from).toList()));
	}

	@GetMapping("/box")
	public Mono<BoxResponse> box(@PathVariable UUID trainerId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size) {
		return listBox.list(new TrainerId(trainerId), page, size).map(BoxResponse::from);
	}

	@PutMapping("/pokemon/{pokemonId}/storage")
	public Mono<PokemonResponse> transfer(@PathVariable UUID trainerId, @PathVariable UUID pokemonId,
			@Valid @RequestBody StorageRequest request) {
		return transferPokemon.transfer(new TrainerId(trainerId), new PokemonId(pokemonId), request.area())
				.map(PokemonResponse::from);
	}

	@PostMapping("/pokemon/{pokemonId}/evolution")
	public Mono<PokemonResponse> evolve(@PathVariable UUID trainerId, @PathVariable UUID pokemonId,
			@Valid @RequestBody EvolutionRequest request) {
		return evolvePokemon.evolve(new TrainerId(trainerId), new PokemonId(pokemonId), request.targetSpecies(),
				Optional.ofNullable(request.ability()))
				.map(PokemonResponse::from);
	}

}
