package com.betwarrior.pokestorage.web.dto;

import java.util.List;
import java.util.UUID;

import com.betwarrior.pokestorage.application.pagination.Page;
import com.betwarrior.pokestorage.domain.trainer.Trainer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class TrainerPayloads {

	private TrainerPayloads() {
	}

	@Schema(description = "Trainer to register")
	public record RegisterTrainerRequest(
			@Schema(description = "Trainer name", example = "Ash") @NotBlank @Size(max = 100) String name) {
	}

	@Schema(description = "A registered trainer")
	public record TrainerResponse(
			@Schema(description = "Trainer id", example = "5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60") UUID id,
			@Schema(description = "Trainer name", example = "Ash") String name) {

		public static TrainerResponse from(Trainer trainer) {
			return new TrainerResponse(trainer.id().value(), trainer.name());
		}

	}

	@Schema(description = "A page of registered trainers")
	public record TrainerPageResponse(
			@Schema(description = "Trainers in this page, in registration order") List<TrainerResponse> trainers,
			@Schema(description = "Zero-based page number", example = "0") int page,
			@Schema(description = "Page size", example = "20") int size,
			@Schema(description = "Registered trainers", example = "1") long totalElements,
			@Schema(description = "Number of pages", example = "1") long totalPages) {

		public static TrainerPageResponse from(Page<Trainer> page) {
			return new TrainerPageResponse(page.map(TrainerResponse::from).content(), page.page(), page.size(),
					page.totalElements(), page.totalPages());
		}

	}

}
