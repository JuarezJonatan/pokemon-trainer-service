package com.betwarrior.pokeapi.model.species;

import java.util.List;
import java.util.Optional;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.evolution.EvolutionChain;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.model.locations.PalParkEncounterArea;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.model.utility.Description;
import com.betwarrior.pokeapi.model.utility.FlavorText;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.ApiRef;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonSpecies(
		int id,
		String name,
		int order,
		int genderRate,
		int captureRate,
		Integer baseHappiness,
		boolean isBaby,
		boolean isLegendary,
		boolean isMythical,
		Integer hatchCounter,
		boolean hasGenderDifferences,
		boolean formsSwitchable,
		NamedRef<GrowthRate> growthRate,
		List<PokemonSpeciesDexEntry> pokedexNumbers,
		List<NamedRef<EggGroup>> eggGroups,
		NamedRef<PokemonColor> color,
		NamedRef<PokemonShape> shape,
		NamedRef<PokemonSpecies> evolvesFromSpecies,
		ApiRef<EvolutionChain> evolutionChain,
		NamedRef<PokemonHabitat> habitat,
		NamedRef<Generation> generation,
		List<Name> names,
		List<FlavorText> flavorTextEntries,
		List<Description> formDescriptions,
		List<Genus> genera,
		List<PokemonSpeciesVariety> varieties,
		List<PalParkEncounterArea> palParkEncounters) implements NamedResource, Localized {

	public PokemonSpecies {
		pokedexNumbers = Lists.nullSafeCopy(pokedexNumbers);
		eggGroups = Lists.nullSafeCopy(eggGroups);
		names = Lists.nullSafeCopy(names);
		flavorTextEntries = Lists.nullSafeCopy(flavorTextEntries);
		formDescriptions = Lists.nullSafeCopy(formDescriptions);
		genera = Lists.nullSafeCopy(genera);
		varieties = Lists.nullSafeCopy(varieties);
		palParkEncounters = Lists.nullSafeCopy(palParkEncounters);
	}

	/**
	 * The variety PokéAPI marks as default (e.g. {@code deoxys-normal} for {@code deoxys}), falling back to the first one.
	 */
	public NamedRef<Pokemon> defaultVariety() {
		return varieties.stream()
				.filter(PokemonSpeciesVariety::isDefault)
				.findFirst()
				.or(() -> varieties.stream().findFirst())
				.map(PokemonSpeciesVariety::pokemon)
				.orElseThrow(() -> new IllegalStateException("Species " + name + " has no varieties"));
	}

	public Optional<NamedRef<PokemonSpecies>> evolvesFrom() {
		return Optional.ofNullable(evolvesFromSpecies);
	}

	public boolean isGenderless() {
		return genderRate == -1;
	}

}
