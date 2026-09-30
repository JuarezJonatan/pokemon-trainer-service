package com.betwarrior.pokestorage.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.betwarrior.pokestorage.application.FindTrainer;
import com.betwarrior.pokestorage.application.RegisterTrainer;
import com.betwarrior.pokestorage.domain.TrainerId;
import com.betwarrior.pokestorage.web.TrainerPayloads.RegisterTrainerRequest;
import com.betwarrior.pokestorage.web.TrainerPayloads.TrainerResponse;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/trainers")
public class TrainerController {

	private final RegisterTrainer registerTrainer;
	private final FindTrainer findTrainer;

	public TrainerController(RegisterTrainer registerTrainer, FindTrainer findTrainer) {
		this.registerTrainer = registerTrainer;
		this.findTrainer = findTrainer;
	}

	@PostMapping
	public Mono<ResponseEntity<TrainerResponse>> register(@Valid @RequestBody RegisterTrainerRequest request,
			UriComponentsBuilder uri) {
		return registerTrainer.register(request.name())
				.map(trainer -> ResponseEntity
						.created(uri.path("/api/v1/trainers/{id}").build(trainer.id().value()))
						.body(TrainerResponse.from(trainer)));
	}

	@GetMapping("/{trainerId}")
	public Mono<TrainerResponse> find(@PathVariable UUID trainerId) {
		return findTrainer.find(new TrainerId(trainerId)).map(TrainerResponse::from);
	}

}
