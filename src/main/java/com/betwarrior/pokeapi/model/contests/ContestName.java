package com.betwarrior.pokeapi.model.contests;

import com.betwarrior.pokeapi.model.utility.Language;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ContestName(
		String name,
		String color,
		NamedRef<Language> language) {

}
