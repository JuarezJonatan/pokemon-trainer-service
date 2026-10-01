package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.ref.NamedRef;

public record VersionGroupFlavorText(
		String text,
		NamedRef<Language> language,
		NamedRef<VersionGroup> versionGroup) {

}
