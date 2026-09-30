package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record PokemonForm(
		int id,
		String name,
		int order,
		Integer formOrder,
		boolean isDefault,
		boolean isBattleOnly,
		String formName,
		NamedRef<Pokemon> pokemon,
		PokemonFormSprites sprites,
		NamedRef<VersionGroup> versionGroup,
		List<Name> names,
		List<Name> formNames,
		boolean isMega,
		List<PokemonFormType> types) implements NamedResource, Localized {

	public PokemonForm {
		names = Lists.nullSafeCopy(names);
		formNames = Lists.nullSafeCopy(formNames);
		types = Lists.nullSafeCopy(types);
	}

}
