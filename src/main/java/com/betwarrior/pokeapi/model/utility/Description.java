package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.ref.NamedRef;

public record Description(
		String description,
		NamedRef<Language> language) {

}
