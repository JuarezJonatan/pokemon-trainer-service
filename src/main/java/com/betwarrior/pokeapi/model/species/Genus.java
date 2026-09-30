package com.betwarrior.pokeapi.model.species;

import com.betwarrior.pokeapi.model.utility.Language;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Genus(
		String genus,
		NamedRef<Language> language) {

}
