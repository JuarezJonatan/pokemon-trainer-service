package com.betwarrior.pokeapi.model.species;

import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonSpeciesVariety(
		boolean isDefault,
		NamedRef<Pokemon> pokemon) {

}
