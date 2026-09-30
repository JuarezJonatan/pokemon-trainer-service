package com.betwarrior.pokestorage.web;

import java.util.UUID;

import com.betwarrior.pokestorage.domain.Trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

final class TrainerPayloads {

	private TrainerPayloads() {
	}

	record RegisterTrainerRequest(@NotBlank @Size(max = 100) String name) {
	}

	record TrainerResponse(UUID id, String name) {

		static TrainerResponse from(Trainer trainer) {
			return new TrainerResponse(trainer.id().value(), trainer.name());
		}

	}

}
