package com.betwarrior.pokestorage.web;

import java.util.UUID;

import com.betwarrior.pokestorage.domain.Trainer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

final class TrainerPayloads {

	private TrainerPayloads() {
	}

	@Schema(description = "Trainer to register")
	record RegisterTrainerRequest(
			@Schema(description = "Trainer name", example = "Ash") @NotBlank @Size(max = 100) String name) {
	}

	@Schema(description = "A registered trainer")
	record TrainerResponse(
			@Schema(description = "Trainer id", example = "5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60") UUID id,
			@Schema(description = "Trainer name", example = "Ash") String name) {

		static TrainerResponse from(Trainer trainer) {
			return new TrainerResponse(trainer.id().value(), trainer.name());
		}

	}

}
