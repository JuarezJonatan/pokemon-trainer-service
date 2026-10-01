package com.betwarrior.pokeapi.model.moves;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.abilities.AbilityEffectChange;
import com.betwarrior.pokeapi.model.contests.ContestEffect;
import com.betwarrior.pokeapi.model.contests.ContestType;
import com.betwarrior.pokeapi.model.contests.SuperContestEffect;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.model.pokemon.Type;
import com.betwarrior.pokeapi.model.utility.Localized;
import com.betwarrior.pokeapi.model.utility.MachineVersionDetail;
import com.betwarrior.pokeapi.model.utility.Name;
import com.betwarrior.pokeapi.model.utility.VerboseEffect;
import com.betwarrior.pokeapi.ref.ApiRef;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Move(
		int id,
		String name,
		Integer accuracy,
		Integer effectChance,
		Integer pp,
		Integer priority,
		Integer power,
		ContestComboSets contestCombos,
		NamedRef<ContestType> contestType,
		ApiRef<ContestEffect> contestEffect,
		NamedRef<MoveDamageClass> damageClass,
		List<VerboseEffect> effectEntries,
		List<AbilityEffectChange> effectChanges,
		List<MoveFlavorText> flavorTextEntries,
		NamedRef<Generation> generation,
		List<MachineVersionDetail> machines,
		MoveMetaData meta,
		List<Name> names,
		List<PastMoveStatValues> pastValues,
		List<MoveStatChange> statChanges,
		ApiRef<SuperContestEffect> superContestEffect,
		NamedRef<MoveTarget> target,
		NamedRef<Type> type,
		List<NamedRef<Pokemon>> learnedByPokemon) implements NamedResource, Localized {

	public Move {
		effectEntries = Lists.nullSafeCopy(effectEntries);
		effectChanges = Lists.nullSafeCopy(effectChanges);
		flavorTextEntries = Lists.nullSafeCopy(flavorTextEntries);
		machines = Lists.nullSafeCopy(machines);
		names = Lists.nullSafeCopy(names);
		pastValues = Lists.nullSafeCopy(pastValues);
		statChanges = Lists.nullSafeCopy(statChanges);
		learnedByPokemon = Lists.nullSafeCopy(learnedByPokemon);
	}

}
