package com.betwarrior.pokestorage.infrastructure.pokeapi;

import java.time.Duration;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.betwarrior.pokestorage.application.exception.CatalogUnavailableException;
import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.domain.species.GenderRatio;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.species.SpeciesAbility;
import com.betwarrior.pokestorage.domain.species.SpeciesRef;
import com.betwarrior.pokestorage.domain.stats.Stat;
import com.betwarrior.pokestorage.domain.stats.StatValues;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import skaro.pokeapi.client.PokeApiClient;
import skaro.pokeapi.resource.NamedApiResource;
import skaro.pokeapi.resource.pokemon.Pokemon;
import skaro.pokeapi.resource.pokemon.PokemonType;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpecies;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpeciesVariety;

/**
 * Translates PokeAPI resources, fetched through the inherited pokeapi-reactor client, into the
 * domain's view of species and items. The domain never sees PokeAPI types.
 */
@Component
public class PokeApiPokemonCatalog implements PokemonCatalog {

	private static final Map<String, Stat> STATS = Map.of(
			"hp", Stat.HP,
			"attack", Stat.ATTACK,
			"defense", Stat.DEFENSE,
			"special-attack", Stat.SPECIAL_ATTACK,
			"special-defense", Stat.SPECIAL_DEFENSE,
			"speed", Stat.SPEED);

	private final PokeApiClient client;
	private final Retry transientFailures;

	@Autowired
	public PokeApiPokemonCatalog(PokeApiClient client) {
		this(client, Duration.ofMillis(200));
	}

	PokeApiPokemonCatalog(PokeApiClient client, Duration firstRetryBackoff) {
		this.client = client;
		this.transientFailures = Retry.backoff(2, firstRetryBackoff).filter(PokeApiPokemonCatalog::isTransient);
	}

	@Override
	public Mono<Species> findSpecies(String name) {
		return client.getResource(PokemonSpecies.class, name)
				.flatMap(species -> client.followResource(() -> defaultVariety(species), Pokemon.class)
						.map(pokemon -> toSpecies(species, pokemon)))
				.retryWhen(transientFailures)
				.onErrorMap(translateFailure("species", name));
	}

	@Override
	public Mono<Item> findItem(String name) {
		return client.getResource(skaro.pokeapi.resource.item.Item.class, name)
				.map(item -> new Item(item.getName(), item.getCategory().getName()))
				.retryWhen(transientFailures)
				.onErrorMap(translateFailure("item", name));
	}

	private static NamedApiResource<Pokemon> defaultVariety(PokemonSpecies species) {
		return species.getVarieties().stream()
				.filter(variety -> Boolean.TRUE.equals(variety.getIsDefault()))
				.findFirst()
				.or(() -> species.getVarieties().stream().findFirst())
				.map(PokemonSpeciesVariety::getPokemon)
				.orElseThrow(() -> new IllegalStateException("Species " + species.getName() + " has no varieties"));
	}

	private static Species toSpecies(PokemonSpecies species, Pokemon pokemon) {
		List<SpeciesAbility> abilities = pokemon.getAbilities().stream()
				.map(ability -> new SpeciesAbility(ability.getAbility().getName(), ability.getSlot(),
						Boolean.TRUE.equals(ability.getIsHidden())))
				.toList();
		Set<String> moves = pokemon.getMoves().stream()
				.map(move -> move.getMove().getName())
				.collect(Collectors.toSet());
		List<String> types = pokemon.getTypes().stream()
				.sorted(Comparator.comparing(PokemonType::getSlot))
				.map(type -> type.getType().getName())
				.toList();
		Map<Stat, Integer> baseStats = new EnumMap<>(Stat.class);
		pokemon.getStats().forEach(stat -> baseStats.put(STATS.get(stat.getStat().getName()), stat.getBaseStat()));
		return new Species(
				new SpeciesRef(species.getId(), species.getName()),
				abilities,
				moves,
				new GenderRatio(species.getGenderRate()),
				new StatValues(baseStats.get(Stat.HP), baseStats.get(Stat.ATTACK), baseStats.get(Stat.DEFENSE),
						baseStats.get(Stat.SPECIAL_ATTACK), baseStats.get(Stat.SPECIAL_DEFENSE), baseStats.get(Stat.SPEED)),
				types,
				pokemon.getSprites() == null ? null : pokemon.getSprites().getFrontDefault(),
				Optional.ofNullable(species.getEvolvesFromSpecies()).map(NamedApiResource::getName));
	}

	private static boolean isTransient(Throwable error) {
		return error instanceof WebClientRequestException
				|| error instanceof TimeoutException
				|| error instanceof WebClientResponseException response && response.getStatusCode().is5xxServerError();
	}

	private static Function<Throwable, Throwable> translateFailure(String kind, String name) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound) {
				return new UnknownCatalogEntryException(kind, name);
			}
			if (error instanceof UnknownCatalogEntryException) {
				return error;
			}
			return new CatalogUnavailableException("PokeAPI could not provide %s '%s'".formatted(kind, name),
					withoutRetryWrapper(error));
		};
	}

	private static Throwable withoutRetryWrapper(Throwable error) {
		return Exceptions.isRetryExhausted(error) && error.getCause() != null ? error.getCause() : error;
	}

}
