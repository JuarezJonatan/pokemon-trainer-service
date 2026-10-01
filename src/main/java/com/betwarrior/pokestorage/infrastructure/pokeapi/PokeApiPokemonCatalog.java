package com.betwarrior.pokestorage.infrastructure.pokeapi;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.betwarrior.pokeapi.PokeApi;
import com.betwarrior.pokeapi.error.ResourceNotFoundException;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.ref.NamedRef;
import com.betwarrior.pokestorage.application.exception.CatalogUnavailableException;
import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.domain.species.GenderRatio;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.species.SpeciesAbility;
import com.betwarrior.pokestorage.domain.species.SpeciesRef;
import com.betwarrior.pokestorage.domain.stats.StatValues;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Translates PokeAPI resources into the domain's view of species and items. The domain never sees PokeAPI types.
 * Retries, timeouts and caching are handled by the {@link PokeApi} client.
 */
@Component
@RequiredArgsConstructor
public class PokeApiPokemonCatalog implements PokemonCatalog {

	private final PokeApi pokeApi;

	@Override
	public Mono<Species> findSpecies(String name) {
		return pokeApi.pokemonSpecies(name)
				.flatMap(species -> pokeApi.pokemon(String.valueOf(species.defaultVariety().id()))
						.map(pokemon -> toSpecies(species, pokemon)))
				.onErrorMap(translateFailure("species", name));
	}

	@Override
	public Mono<Item> findItem(String name) {
		return pokeApi.item(name)
				.map(item -> new Item(item.name(), item.category().name()))
				.onErrorMap(translateFailure("item", name));
	}

	private static Species toSpecies(PokemonSpecies species, Pokemon pokemon) {
		List<SpeciesAbility> abilities = pokemon.abilities().stream()
				.map(ability -> new SpeciesAbility(ability.ability().name(), ability.slot(), ability.isHidden()))
				.toList();
		Map<String, Integer> baseStats = pokemon.baseStats();
		return new Species(
				new SpeciesRef(species.id(), species.name()),
				abilities,
				pokemon.moveNames(),
				new GenderRatio(species.genderRate()),
				new StatValues(baseStats.get("hp"), baseStats.get("attack"), baseStats.get("defense"),
						baseStats.get("special-attack"), baseStats.get("special-defense"), baseStats.get("speed")),
				pokemon.typeNames(),
				pokemon.frontSprite().orElse(null),
				species.evolvesFrom().map(NamedRef::name));
	}

	private static Function<Throwable, Throwable> translateFailure(String kind, String name) {
		return error -> error instanceof ResourceNotFoundException
				? new UnknownCatalogEntryException(kind, name)
				: new CatalogUnavailableException("PokeAPI could not provide %s '%s'".formatted(kind, name), error);
	}

}
