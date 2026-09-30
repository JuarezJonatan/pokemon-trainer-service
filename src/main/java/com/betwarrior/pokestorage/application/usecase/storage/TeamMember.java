package com.betwarrior.pokestorage.application.usecase.storage;

import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.stats.StatValues;

public record TeamMember(PokemonSpecimen pokemon, Species species, StatValues stats) {

	static TeamMember of(PokemonSpecimen pokemon, Species species) {
		StatValues stats = species.statsAt(pokemon.level(), pokemon.individualValues(), pokemon.effortValues(),
				pokemon.nature());
		return new TeamMember(pokemon, species, stats);
	}

}
