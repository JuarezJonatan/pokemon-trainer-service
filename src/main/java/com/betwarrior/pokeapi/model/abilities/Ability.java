package com.betwarrior.pokeapi.model.abilities;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.model.utility.VerboseEffect;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Ability(
		int id,
		String name,
		boolean isMainSeries,
		NamedRef<Generation> generation,
		List<Name> names,
		List<VerboseEffect> effectEntries,
		List<AbilityEffectChange> effectChanges,
		List<AbilityFlavorText> flavorTextEntries,
		List<AbilityPokemon> pokemon) implements NamedResource, Localized {

	public Ability {
		names = Lists.nullSafeCopy(names);
		effectEntries = Lists.nullSafeCopy(effectEntries);
		effectChanges = Lists.nullSafeCopy(effectChanges);
		flavorTextEntries = Lists.nullSafeCopy(flavorTextEntries);
		pokemon = Lists.nullSafeCopy(pokemon);
	}

}
