package com.betwarrior.pokeapi.model.pokemon;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.ref.NamedRef;

public record TypeRelations(
		List<NamedRef<Type>> noDamageTo,
		List<NamedRef<Type>> halfDamageTo,
		List<NamedRef<Type>> doubleDamageTo,
		List<NamedRef<Type>> noDamageFrom,
		List<NamedRef<Type>> halfDamageFrom,
		List<NamedRef<Type>> doubleDamageFrom) {

	public TypeRelations {
		noDamageTo = Lists.nullSafeCopy(noDamageTo);
		halfDamageTo = Lists.nullSafeCopy(halfDamageTo);
		doubleDamageTo = Lists.nullSafeCopy(doubleDamageTo);
		noDamageFrom = Lists.nullSafeCopy(noDamageFrom);
		halfDamageFrom = Lists.nullSafeCopy(halfDamageFrom);
		doubleDamageFrom = Lists.nullSafeCopy(doubleDamageFrom);
	}

}
