package com.betwarrior.pokeapi.model.moves;

import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.utility.Language;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveFlavorText(
		String flavorText,
		NamedRef<Language> language,
		NamedRef<VersionGroup> versionGroup) {

}
