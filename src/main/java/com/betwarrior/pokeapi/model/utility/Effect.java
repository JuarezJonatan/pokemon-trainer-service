package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.ref.NamedRef;

public record Effect(
		String effect,
		NamedRef<Language> language) {

}
