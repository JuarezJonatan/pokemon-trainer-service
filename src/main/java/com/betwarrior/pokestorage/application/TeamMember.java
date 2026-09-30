package com.betwarrior.pokestorage.application;

import com.betwarrior.pokestorage.domain.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.Species;
import com.betwarrior.pokestorage.domain.StatValues;

public record TeamMember(PokemonSpecimen pokemon, Species species, StatValues stats) {

	static TeamMember of(PokemonSpecimen pokemon, Species species) {
		StatValues stats = species.statsAt(pokemon.level(), pokemon.individualValues(), pokemon.effortValues(),
				pokemon.nature());
		return new TeamMember(pokemon, species, stats);
	}

}
