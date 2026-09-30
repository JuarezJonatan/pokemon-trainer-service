package com.betwarrior.pokeapi.model.species;

import com.betwarrior.pokeapi.model.utility.Language;
import com.betwarrior.pokeapi.ref.NamedRef;

public record AwesomeName(
		String awesomeName,
		NamedRef<Language> language) {

}
