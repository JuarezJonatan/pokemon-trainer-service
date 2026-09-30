package com.betwarrior.pokestorage.web.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.betwarrior.pokestorage.application.usecase.trainer.FindTrainer;
import com.betwarrior.pokestorage.application.usecase.trainer.RegisterTrainer;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;
import com.betwarrior.pokestorage.web.dto.TrainerPayloads.RegisterTrainerRequest;
import com.betwarrior.pokestorage.web.dto.TrainerPayloads.TrainerResponse;
import com.betwarrior.pokestorage.web.openapi.OpenApiConfiguration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/trainers")
@Tag(name = OpenApiConfiguration.TRAINERS_TAG)
public class TrainerController {

	private final RegisterTrainer registerTrainer;
	private final FindTrainer findTrainer;

	public TrainerController(RegisterTrainer registerTrainer, FindTrainer findTrainer) {
		this.registerTrainer = registerTrainer;
		this.findTrainer = findTrainer;
	}

	@Operation(summary = "Register a trainer",
			description = "Creates a trainer with an empty team and box. Its id is the one every other endpoint expects.")
	@ApiResponse(responseCode = "201", description = "Trainer registered",
			headers = @Header(name = "Location", description = "URL of the new trainer",
					schema = @Schema(type = "string", format = "uri")))
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@PostMapping
	public Mono<ResponseEntity<TrainerResponse>> register(@Valid @RequestBody RegisterTrainerRequest request,
			UriComponentsBuilder uri) {
		return registerTrainer.register(request.name())
				.map(trainer -> ResponseEntity
						.created(uri.path("/api/v1/trainers/{id}").build(trainer.id().value()))
						.body(TrainerResponse.from(trainer)));
	}

	@Operation(summary = "Get a trainer")
	@ApiResponse(responseCode = "200", description = "The trainer")
	@ApiResponse(responseCode = "400", ref = OpenApiConfiguration.BAD_REQUEST)
	@ApiResponse(responseCode = "404", ref = OpenApiConfiguration.NOT_FOUND)
	@GetMapping("/{trainerId}")
	public Mono<TrainerResponse> find(
			@Parameter(description = "Trainer id", example = "5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60") @PathVariable UUID trainerId) {
		return findTrainer.find(new TrainerId(trainerId)).map(TrainerResponse::from);
	}

}
