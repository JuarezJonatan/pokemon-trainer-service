package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.berries.BerryFlavor;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Nature(
		int id,
		String name,
		NamedRef<Stat> decreasedStat,
		NamedRef<Stat> increasedStat,
		NamedRef<BerryFlavor> hatesFlavor,
		NamedRef<BerryFlavor> likesFlavor,
		List<Name> names,
		List<MoveBattleStylePreference> moveBattleStylePreferences,
		List<NatureStatChange> pokeathlonStatChanges) implements NamedResource, Localized {

	public Nature {
		names = Lists.nullSafeCopy(names);
		moveBattleStylePreferences = Lists.nullSafeCopy(moveBattleStylePreferences);
		pokeathlonStatChanges = Lists.nullSafeCopy(pokeathlonStatChanges);
	}

}
