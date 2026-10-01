package com.betwarrior.pokeapi.model.evolution;

import java.util.List;

import com.betwarrior.pokeapi.model.Lists;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.ref.NamedRef;

public record ChainLink(
		boolean isBaby,
		NamedRef<PokemonSpecies> species,
		List<EvolutionDetail> evolutionDetails,
		List<ChainLink> evolvesTo) {

	public ChainLink {
		evolutionDetails = Lists.nullSafeCopy(evolutionDetails);
		evolvesTo = Lists.nullSafeCopy(evolvesTo);
	}

}
