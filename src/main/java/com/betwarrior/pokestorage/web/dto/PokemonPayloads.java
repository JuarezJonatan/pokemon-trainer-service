package com.betwarrior.pokestorage.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.betwarrior.pokestorage.application.usecase.pokemon.CapturePokemonCommand;
import com.betwarrior.pokestorage.application.usecase.storage.BoxPage;
import com.betwarrior.pokestorage.application.usecase.storage.TeamMember;
import com.betwarrior.pokestorage.domain.pokemon.Gender;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.stats.Stat;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PokemonPayloads {

	private PokemonPayloads() {
	}

	@Schema(description = "A Pokemon to capture. Names are PokeAPI's (lowercase, hyphenated) and are normalized.")
	public record CaptureRequest(
			@Schema(description = "PokeAPI species name", example = "pikachu")
			@NotBlank String species,
			@Schema(description = "Optional nickname", example = "Sparky", maxLength = 40)
			@Size(max = 40) String nickname,
			@Schema(description = "Current level, also recorded as the met level", example = "25", minimum = "1", maximum = "100")
			@NotNull Integer level,
			@Schema(description = "Individual Values (genetics): each stat between 0 and 31")
			@NotNull @Valid StatsPayload individualValues,
			@Schema(description = "Effort Values (training): each stat between 0 and 252, at most 510 in total. Defaults to all 0")
			@Valid StatsPayload effortValues,
			@Schema(description = "Nature: raises one stat and lowers another by 10% (neutral natures change nothing)", example = "TIMID")
			@NotNull Nature nature,
			@Schema(description = "One of the species' abilities in PokeAPI, hidden abilities included", example = "static")
			@NotBlank String ability,
			@Schema(description = "Must be compatible with the species' gender rate", example = "FEMALE")
			@NotNull Gender gender,
			@Schema(description = "Whether the Pokemon is shiny", example = "true", defaultValue = "false")
			boolean shiny,
			@ArraySchema(arraySchema = @Schema(description = "Between 1 and 4 distinct moves the species can learn"),
					schema = @Schema(example = "thunderbolt"), minItems = 1, maxItems = 4, uniqueItems = true)
			@NotEmpty List<@NotBlank String> moves,
			@Schema(description = "Optional held item; must exist in PokeAPI", example = "light-ball")
			String heldItem,
			@Schema(description = "Where and how the Pokemon was obtained")
			@NotNull @Valid OriginRequest origin) {

		public CapturePokemonCommand toCommand(TrainerId trainer) {
			return new CapturePokemonCommand(trainer, species, Optional.ofNullable(nickname), level,
					individualValues.toStatValues(), Optional.ofNullable(effortValues).map(StatsPayload::toStatValues),
					nature, ability, gender, shiny, Optional.ofNullable(origin.originalTrainerId()), origin.pokeball(),
					Optional.ofNullable(origin.caughtAt()), origin.location(), moves, Optional.ofNullable(heldItem));
		}

	}

	@Schema(description = "Origin data of a captured Pokemon")
	public record OriginRequest(
			@Schema(description = "Original trainer (OT). Defaults to the capturing trainer's id", example = "ASH-0001")
			String originalTrainerId,
			@Schema(description = "Poke Ball used; must be an item of a `*-balls` category in PokeAPI", example = "poke-ball")
			@NotBlank String pokeball,
			@Schema(description = "Capture date. Defaults to now", example = "2026-09-30T12:00:00Z")
			Instant caughtAt,
			@Schema(description = "Where it was met", example = "viridian-forest")
			@NotBlank String location) {
	}

	@Schema(description = "Destination of a transfer")
	public record StorageRequest(
			@Schema(description = "`TEAM` to withdraw from the box, `BOX` to deposit from the team", example = "BOX")
			@NotNull StorageArea area) {
	}

	@Schema(description = "Evolution to perform")
	public record EvolutionRequest(
			@Schema(description = "Direct evolution of the current species", example = "gyarados")
			@NotBlank String targetSpecies,
			@Schema(description = "Optional ability of the target species. If absent, the same ability slot is kept",
					example = "intimidate")
			String ability) {
	}

	@Schema(description = "Species of a Pokemon")
	public record SpeciesSummary(
			@Schema(description = "National Pokedex number", example = "25") int id,
			@Schema(description = "PokeAPI species name", example = "pikachu") String name) {
	}

	@Schema(description = "Nature and its effect on stats")
	public record NatureResponse(
			@Schema(description = "Nature", example = "TIMID") Nature name,
			@Schema(description = "Stat raised by 10%; null for neutral natures", example = "SPEED") Stat increased,
			@Schema(description = "Stat lowered by 10%; null for neutral natures", example = "ATTACK") Stat decreased) {

		public static NatureResponse from(Nature nature) {
			return nature.isNeutral()
					? new NatureResponse(nature, null, null)
					: new NatureResponse(nature, nature.increased(), nature.decreased());
		}

	}

	@Schema(description = "Origin data")
	public record OriginResponse(
			@Schema(description = "Original trainer (OT)", example = "5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60") String originalTrainerId,
			@Schema(description = "Poke Ball used", example = "poke-ball") String pokeball,
			@Schema(description = "Capture date", example = "2026-09-30T12:00:00Z") Instant caughtAt,
			@Schema(description = "Level when captured", example = "25") int metLevel,
			@Schema(description = "Where it was met", example = "viridian-forest") String metLocation) {
	}

	@Schema(description = "Current storage position")
	public record StorageResponse(
			@Schema(description = "Area the Pokemon is in", example = "TEAM") StorageArea area,
			@Schema(description = "1-based slot within the area. Slots are never compacted", example = "1") int slot) {
	}

	@Schema(description = "An individual Pokemon with all its technical, genetic and origin metadata")
	public record PokemonResponse(
			@Schema(description = "Pokemon id", example = "0a9e3d8c-7f41-4b62-a6d3-2c1b0e9f8d7a") UUID id,
			@Schema(description = "Owner trainer id", example = "5f0c2a4e-3b8e-4c1e-9d5a-1b2c3d4e5f60") UUID trainerId,
			@Schema(description = "Species") SpeciesSummary species,
			@Schema(description = "Nickname, null if it has none", example = "Sparky") String nickname,
			@Schema(description = "Current level", example = "25") int level,
			@Schema(description = "Individual Values (0-31 per stat)") StatsPayload individualValues,
			@Schema(description = "Effort Values (0-252 per stat, at most 510 in total)") StatsPayload effortValues,
			@Schema(description = "Nature") NatureResponse nature,
			@Schema(description = "Ability", example = "static") String ability,
			@Schema(description = "Gender", example = "FEMALE") Gender gender,
			@Schema(description = "Whether it is shiny", example = "true") boolean shiny,
			@ArraySchema(arraySchema = @Schema(description = "Known moves (1 to 4)"), schema = @Schema(example = "thunderbolt"))
			List<String> moves,
			@Schema(description = "Held item, null if it holds none", example = "light-ball") String heldItem,
			@Schema(description = "Origin data") OriginResponse origin,
			@Schema(description = "Where it is stored") StorageResponse storage) {

		public static PokemonResponse from(PokemonSpecimen pokemon) {
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

	@Schema(description = "Species data taken from PokeAPI")
	public record SpeciesDetails(
			@Schema(description = "National Pokedex number", example = "25") int id,
			@Schema(description = "PokeAPI species name", example = "pikachu") String name,
			@ArraySchema(arraySchema = @Schema(description = "Types, in slot order"), schema = @Schema(example = "electric"))
			List<String> types,
			@Schema(description = "Base stats of the species") StatsPayload baseStats,
			@Schema(description = "Default front sprite",
					example = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png")
			String spriteUrl) {

		public static SpeciesDetails from(Species species) {
			return new SpeciesDetails(species.ref().id(), species.name(), species.types(),
					StatsPayload.from(species.baseStats()), species.spriteUrl());
		}

	}

	@Schema(description = "A team member combined with its PokeAPI species data")
	public record TeamMemberResponse(
			@Schema(description = "Team slot (1-based)", example = "1") int slot,
			@Schema(description = "The stored Pokemon") PokemonResponse pokemon,
			@Schema(description = "Species data from PokeAPI") SpeciesDetails species,
			@Schema(description = "Actual stats from base stats, IVs, EVs, level and nature (official Gen III+ formula)")
			StatsPayload stats) {

		public static TeamMemberResponse from(TeamMember member) {
			return new TeamMemberResponse(member.pokemon().slot().position(), PokemonResponse.from(member.pokemon()),
					SpeciesDetails.from(member.species()), StatsPayload.from(member.stats()));
		}

	}

	@Schema(description = "The active team as a composite view")
	public record TeamResponse(
			@Schema(description = "Maximum team size", example = "6") int capacity,
			@Schema(description = "Members ordered by slot") List<TeamMemberResponse> members) {
	}

	@Schema(description = "A page of the PC box")
	public record BoxResponse(
			@Schema(description = "Pokemon in this page, ordered by slot") List<PokemonResponse> pokemon,
			@Schema(description = "Zero-based page number", example = "0") int page,
			@Schema(description = "Page size", example = "30") int size,
			@Schema(description = "Pokemon in the box", example = "1") long totalElements,
			@Schema(description = "Number of pages", example = "1") long totalPages) {

		public static BoxResponse from(BoxPage page) {
			return new BoxResponse(page.pokemon().stream().map(PokemonResponse::from).toList(), page.page(),
					page.size(), page.totalElements(), page.totalPages());
		}

	}

}
