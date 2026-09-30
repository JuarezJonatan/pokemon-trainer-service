package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.ref.NamedRef;

public record Name(
		String name,
		NamedRef<Language> language) {

}
