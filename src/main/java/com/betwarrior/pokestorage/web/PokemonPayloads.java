package com.betwarrior.pokestorage.web;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.betwarrior.pokestorage.application.BoxPage;
import com.betwarrior.pokestorage.application.CapturePokemonCommand;
import com.betwarrior.pokestorage.application.TeamMember;
import com.betwarrior.pokestorage.domain.Gender;
import com.betwarrior.pokestorage.domain.Nature;
import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.Species;
import com.betwarrior.pokestorage.domain.Stat;
import com.betwarrior.pokestorage.domain.StorageArea;
import com.betwarrior.pokestorage.domain.TrainerId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

final class PokemonPayloads {

	private PokemonPayloads() {
	}

	record CaptureRequest(
			@NotBlank String species,
			@Size(max = 40) String nickname,
			@NotNull Integer level,
			@NotNull @Valid StatsPayload individualValues,
			@Valid StatsPayload effortValues,
			@NotNull Nature nature,
			@NotBlank String ability,
			@NotNull Gender gender,
			boolean shiny,
			@NotEmpty List<@NotBlank String> moves,
			String heldItem,
			@NotNull @Valid OriginRequest origin) {

		CapturePokemonCommand toCommand(TrainerId trainer) {
			return new CapturePokemonCommand(trainer, species, Optional.ofNullable(nickname), level,
					individualValues.toStatValues(), Optional.ofNullable(effortValues).map(StatsPayload::toStatValues),
					nature, ability, gender, shiny, Optional.ofNullable(origin.originalTrainerId()), origin.pokeball(),
					Optional.ofNullable(origin.caughtAt()), origin.location(), moves, Optional.ofNullable(heldItem));
		}

	}

	record OriginRequest(String originalTrainerId, @NotBlank String pokeball, Instant caughtAt,
			@NotBlank String location) {
	}

	record StorageRequest(@NotNull StorageArea area) {
	}

	record EvolutionRequest(@NotBlank String targetSpecies, String ability) {
	}

	record SpeciesSummary(int id, String name) {
	}

	record NatureResponse(Nature name, Stat increased, Stat decreased) {

		static NatureResponse from(Nature nature) {
			return nature.isNeutral()
					? new NatureResponse(nature, null, null)
					: new NatureResponse(nature, nature.increased(), nature.decreased());
		}

	}

	record OriginResponse(String originalTrainerId, String pokeball, Instant caughtAt, int metLevel,
			String metLocation) {
	}

	record StorageResponse(StorageArea area, int slot) {
	}

	record PokemonResponse(
			UUID id,
			UUID trainerId,
			SpeciesSummary species,
			String nickname,
			int level,
			StatsPayload individualValues,
			StatsPayload effortValues,
			NatureResponse nature,
			String ability,
			Gender gender,
			boolean shiny,
			List<String> moves,
			String heldItem,
			OriginResponse origin,
			StorageResponse storage) {

		static PokemonResponse from(PokemonSpecimen pokemon) {
			return new PokemonResponse(
					pokemon.id().value(),
					pokemon.owner().value(),
					new SpeciesSummary(pokemon.species().id(), pokemon.species().name()),
					pokemon.nickname().orElse(null),
					pokemon.level().value(),
					StatsPayload.from(pokemon.individualValues().values()),
					StatsPayload.from(pokemon.effortValues().values()),
					NatureResponse.from(pokemon.nature()),
					pokemon.ability(),
					pokemon.gender(),
					pokemon.shiny(),
					pokemon.moves().moves(),
					pokemon.heldItem().orElse(null),
					new OriginResponse(pokemon.origin().originalTrainerId(), pokemon.origin().pokeball(),
							pokemon.origin().caughtAt(), pokemon.origin().metLevel().value(),
							pokemon.origin().metLocation()),
					new StorageResponse(pokemon.slot().area(), pokemon.slot().position()));
		}

	}

	record SpeciesDetails(int id, String name, List<String> types, StatsPayload baseStats, String spriteUrl) {

		static SpeciesDetails from(Species species) {
			return new SpeciesDetails(species.ref().id(), species.name(), species.types(),
					StatsPayload.from(species.baseStats()), species.spriteUrl());
		}

	}

	record TeamMemberResponse(int slot, PokemonResponse pokemon, SpeciesDetails species, StatsPayload stats) {

		static TeamMemberResponse from(TeamMember member) {
			return new TeamMemberResponse(member.pokemon().slot().position(), PokemonResponse.from(member.pokemon()),
					SpeciesDetails.from(member.species()), StatsPayload.from(member.stats()));
		}

	}

	record TeamResponse(int capacity, List<TeamMemberResponse> members) {
	}

	record BoxResponse(List<PokemonResponse> pokemon, int page, int size, long totalElements, long totalPages) {

		static BoxResponse from(BoxPage page) {
			return new BoxResponse(page.pokemon().stream().map(PokemonResponse::from).toList(), page.page(),
					page.size(), page.totalElements(), page.totalPages());
		}

	}

}
