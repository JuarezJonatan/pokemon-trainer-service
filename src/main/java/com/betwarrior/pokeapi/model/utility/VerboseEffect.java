package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.ref.NamedRef;

public record VerboseEffect(
		String effect,
		String shortEffect,
		NamedRef<Language> language) {

}
