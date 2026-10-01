package com.betwarrior.pokestorage.testsupport;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.betwarrior.pokestorage.domain.pokemon.CaptureOrigin;
import com.betwarrior.pokestorage.domain.pokemon.Gender;
import com.betwarrior.pokestorage.domain.pokemon.Level;
import com.betwarrior.pokestorage.domain.pokemon.MoveSet;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.species.GenderRatio;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.species.SpeciesAbility;
import com.betwarrior.pokestorage.domain.species.SpeciesRef;
import com.betwarrior.pokestorage.domain.stats.EffortValues;
import com.betwarrior.pokestorage.domain.stats.IndividualValues;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.stats.StatValues;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

public final class PokemonFixtures {

	public static final TrainerId ASH = TrainerId.random();

	private PokemonFixtures() {
	}

	public static StatValues stats(int value) {
		return new StatValues(value, value, value, value, value, value);
	}

	public static Species pikachu() {
		return new Species(new SpeciesRef(25, "pikachu"),
				List.of(new SpeciesAbility("static", 1, false), new SpeciesAbility("lightning-rod", 3, true)),
				Set.of("thunder-shock", "quick-attack", "thunderbolt", "iron-tail"),
				new GenderRatio(4), new StatValues(35, 55, 40, 50, 50, 90), List.of("electric"),
				"https://sprites/25.png", Optional.of("pichu"));
	}

	public static Species raichu() {
		return new Species(new SpeciesRef(26, "raichu"),
				List.of(new SpeciesAbility("static", 1, false), new SpeciesAbility("lightning-rod", 3, true)),
				Set.of("thunder-shock", "thunderbolt"), new GenderRatio(4), new StatValues(60, 90, 55, 90, 80, 110),
				List.of("electric"), "https://sprites/26.png", Optional.of("pikachu"));
	}

	public static Species magikarp() {
		return new Species(new SpeciesRef(129, "magikarp"),
				List.of(new SpeciesAbility("swift-swim", 1, false), new SpeciesAbility("rattled", 3, true)),
				Set.of("splash", "tackle"), new GenderRatio(4), new StatValues(20, 10, 55, 15, 20, 80),
				List.of("water"), "https://sprites/129.png", Optional.empty());
	}

	public static Species gyarados() {
		return new Species(new SpeciesRef(130, "gyarados"),
				List.of(new SpeciesAbility("intimidate", 1, false), new SpeciesAbility("moxie", 3, true)),
				Set.of("splash", "tackle", "bite"), new GenderRatio(4), new StatValues(95, 125, 79, 60, 100, 81),
				List.of("water", "flying"), "https://sprites/130.png", Optional.of("magikarp"));
	}

	public static Species eevee() {
		return new Species(new SpeciesRef(133, "eevee"),
				List.of(new SpeciesAbility("run-away", 1, false), new SpeciesAbility("adaptability", 2, false),
						new SpeciesAbility("anticipation", 3, true)),
				Set.of("tackle"), new GenderRatio(1), stats(55), List.of("normal"), "https://sprites/133.png",
				Optional.empty());
	}

	public static Species vaporeon() {
		return new Species(new SpeciesRef(134, "vaporeon"),
				List.of(new SpeciesAbility("water-absorb", 1, false), new SpeciesAbility("hydration", 3, true)),
				Set.of("tackle", "water-gun"), new GenderRatio(1), stats(65), List.of("water"),
				"https://sprites/134.png", Optional.of("eevee"));
	}

	public static Species magnemite() {
		return new Species(new SpeciesRef(81, "magnemite"), List.of(new SpeciesAbility("sturdy", 1, false)),
				Set.of("tackle"), GenderRatio.GENDERLESS, stats(40), List.of("electric", "steel"),
				"https://sprites/81.png", Optional.empty());
	}

	public static PokemonSpecimen specimenOf(Species species, String ability, StorageSlot slot) {
		return new PokemonSpecimen(PokemonId.random(), ASH, species.ref(), Optional.of("Sparky"), new Level(25),
				new IndividualValues(stats(31)), new EffortValues(new StatValues(252, 0, 0, 6, 0, 252)), Nature.TIMID,
				ability, Gender.FEMALE, true,
				new CaptureOrigin(ASH.toString(), "ultra-ball", Instant.parse("2026-09-01T10:00:00Z"), new Level(5),
						"viridian-forest"),
				new MoveSet(List.of("tackle")), Optional.of("light-ball"), slot, PokemonSpecimen.FIRST_VERSION);
	}

	public static PokemonSpecimen withoutNicknameNorItem(PokemonSpecimen p) {
		return new PokemonSpecimen(p.id(), p.owner(), p.species(), Optional.empty(), p.level(), p.individualValues(),
				p.effortValues(), p.nature(), p.ability(), p.gender(), p.shiny(), p.origin(), p.moves(), Optional.empty(),
				p.slot(), p.version());
	}

	public static PokemonSpecimen ownedBy(PokemonSpecimen p, TrainerId owner) {
		return new PokemonSpecimen(p.id(), owner, p.species(), p.nickname(), p.level(), p.individualValues(),
				p.effortValues(), p.nature(), p.ability(), p.gender(), p.shiny(), p.origin(), p.moves(), p.heldItem(),
				p.slot(), p.version());
	}

}
