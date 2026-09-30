package com.betwarrior.pokeapi;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.betwarrior.pokeapi.model.abilities.Ability;
import com.betwarrior.pokeapi.model.berries.Berry;
import com.betwarrior.pokeapi.model.berries.BerryFirmness;
import com.betwarrior.pokeapi.model.berries.BerryFlavor;
import com.betwarrior.pokeapi.model.contests.ContestEffect;
import com.betwarrior.pokeapi.model.contests.ContestType;
import com.betwarrior.pokeapi.model.contests.SuperContestEffect;
import com.betwarrior.pokeapi.model.encounters.EncounterCondition;
import com.betwarrior.pokeapi.model.encounters.EncounterConditionValue;
import com.betwarrior.pokeapi.model.encounters.EncounterMethod;
import com.betwarrior.pokeapi.model.evolution.EvolutionChain;
import com.betwarrior.pokeapi.model.evolution.EvolutionTrigger;
import com.betwarrior.pokeapi.model.evolution.EvolutionVariable;
import com.betwarrior.pokeapi.model.games.Generation;
import com.betwarrior.pokeapi.model.games.Pokedex;
import com.betwarrior.pokeapi.model.games.Version;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.items.Currency;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.model.items.ItemAttribute;
import com.betwarrior.pokeapi.model.items.ItemCategory;
import com.betwarrior.pokeapi.model.items.ItemFlingEffect;
import com.betwarrior.pokeapi.model.items.ItemPocket;
import com.betwarrior.pokeapi.model.locations.Location;
import com.betwarrior.pokeapi.model.locations.LocationArea;
import com.betwarrior.pokeapi.model.locations.PalParkArea;
import com.betwarrior.pokeapi.model.locations.Region;
import com.betwarrior.pokeapi.model.machines.Machine;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.model.moves.MoveAilment;
import com.betwarrior.pokeapi.model.moves.MoveBattleStyle;
import com.betwarrior.pokeapi.model.moves.MoveCategory;
import com.betwarrior.pokeapi.model.moves.MoveDamageClass;
import com.betwarrior.pokeapi.model.moves.MoveLearnMethod;
import com.betwarrior.pokeapi.model.moves.MoveTarget;
import com.betwarrior.pokeapi.model.pokemon.Characteristic;
import com.betwarrior.pokeapi.model.pokemon.Nature;
import com.betwarrior.pokeapi.model.pokemon.PokeathlonStat;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.model.pokemon.PokemonForm;
import com.betwarrior.pokeapi.model.pokemon.Stat;
import com.betwarrior.pokeapi.model.pokemon.Type;
import com.betwarrior.pokeapi.model.species.EggGroup;
import com.betwarrior.pokeapi.model.species.Gender;
import com.betwarrior.pokeapi.model.species.GrowthRate;
import com.betwarrior.pokeapi.model.species.PokemonColor;
import com.betwarrior.pokeapi.model.species.PokemonHabitat;
import com.betwarrior.pokeapi.model.species.PokemonShape;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.model.utility.Language;
import com.betwarrior.pokeapi.ref.NamedRef;

import reactor.core.publisher.Mono;

/**
 * Declarative PokéAPI v2 client. Spring implements it on top of {@code WebClient} (see
 * {@link PokeApiAutoConfiguration}); inject it and call the endpoint you need, e.g.
 * {@code pokeApi.pokemon("pikachu")}.
 * <p>
 * Any Pokémon, item or move PokéAPI knows is reachable without code changes. Only a new kind of resource needs a
 * record and one method here. Responses are cached per endpoint when the application enables caching.
 */
@HttpExchange(accept = "application/json")
@CacheConfig(keyGenerator = PokeApiAutoConfiguration.KEY_GENERATOR)
public interface PokeApi {

	/**
	 * A page of any endpoint's listing, e.g. {@code list("pokemon", 0, 20)}. Resources without a name (evolution
	 * chains, machines, ...) come with a {@code null} name.
	 */
	@GetExchange("/{endpoint}")
	@Cacheable("pokeapi.listings")
	Mono<Page<NamedRef<Object>>> list(@PathVariable String endpoint, @RequestParam int offset, @RequestParam int limit);

	@GetExchange("/ability/{nameOrId}")
	@Cacheable("pokeapi.ability")
	Mono<Ability> ability(@PathVariable String nameOrId);

	@GetExchange("/berry/{nameOrId}")
	@Cacheable("pokeapi.berry")
	Mono<Berry> berry(@PathVariable String nameOrId);

	@GetExchange("/berry-firmness/{nameOrId}")
	@Cacheable("pokeapi.berry-firmness")
	Mono<BerryFirmness> berryFirmness(@PathVariable String nameOrId);

	@GetExchange("/berry-flavor/{nameOrId}")
	@Cacheable("pokeapi.berry-flavor")
	Mono<BerryFlavor> berryFlavor(@PathVariable String nameOrId);

	@GetExchange("/characteristic/{id}")
	@Cacheable("pokeapi.characteristic")
	Mono<Characteristic> characteristic(@PathVariable int id);

	@GetExchange("/contest-effect/{id}")
	@Cacheable("pokeapi.contest-effect")
	Mono<ContestEffect> contestEffect(@PathVariable int id);

	@GetExchange("/contest-type/{nameOrId}")
	@Cacheable("pokeapi.contest-type")
	Mono<ContestType> contestType(@PathVariable String nameOrId);

	@GetExchange("/currency/{nameOrId}")
	@Cacheable("pokeapi.currency")
	Mono<Currency> currency(@PathVariable String nameOrId);

	@GetExchange("/egg-group/{nameOrId}")
	@Cacheable("pokeapi.egg-group")
	Mono<EggGroup> eggGroup(@PathVariable String nameOrId);

	@GetExchange("/encounter-condition/{nameOrId}")
	@Cacheable("pokeapi.encounter-condition")
	Mono<EncounterCondition> encounterCondition(@PathVariable String nameOrId);

	@GetExchange("/encounter-condition-value/{nameOrId}")
	@Cacheable("pokeapi.encounter-condition-value")
	Mono<EncounterConditionValue> encounterConditionValue(@PathVariable String nameOrId);

	@GetExchange("/encounter-method/{nameOrId}")
	@Cacheable("pokeapi.encounter-method")
	Mono<EncounterMethod> encounterMethod(@PathVariable String nameOrId);

	@GetExchange("/evolution-chain/{id}")
	@Cacheable("pokeapi.evolution-chain")
	Mono<EvolutionChain> evolutionChain(@PathVariable int id);

	@GetExchange("/evolution-trigger/{nameOrId}")
	@Cacheable("pokeapi.evolution-trigger")
	Mono<EvolutionTrigger> evolutionTrigger(@PathVariable String nameOrId);

	@GetExchange("/evolution-variable/{nameOrId}")
	@Cacheable("pokeapi.evolution-variable")
	Mono<EvolutionVariable> evolutionVariable(@PathVariable String nameOrId);

	@GetExchange("/gender/{nameOrId}")
	@Cacheable("pokeapi.gender")
	Mono<Gender> gender(@PathVariable String nameOrId);

	@GetExchange("/generation/{nameOrId}")
	@Cacheable("pokeapi.generation")
	Mono<Generation> generation(@PathVariable String nameOrId);

	@GetExchange("/growth-rate/{nameOrId}")
	@Cacheable("pokeapi.growth-rate")
	Mono<GrowthRate> growthRate(@PathVariable String nameOrId);

	@GetExchange("/item/{nameOrId}")
	@Cacheable("pokeapi.item")
	Mono<Item> item(@PathVariable String nameOrId);

	@GetExchange("/item-attribute/{nameOrId}")
	@Cacheable("pokeapi.item-attribute")
	Mono<ItemAttribute> itemAttribute(@PathVariable String nameOrId);

	@GetExchange("/item-category/{nameOrId}")
	@Cacheable("pokeapi.item-category")
	Mono<ItemCategory> itemCategory(@PathVariable String nameOrId);

	@GetExchange("/item-fling-effect/{nameOrId}")
	@Cacheable("pokeapi.item-fling-effect")
	Mono<ItemFlingEffect> itemFlingEffect(@PathVariable String nameOrId);

	@GetExchange("/item-pocket/{nameOrId}")
	@Cacheable("pokeapi.item-pocket")
	Mono<ItemPocket> itemPocket(@PathVariable String nameOrId);

	@GetExchange("/language/{nameOrId}")
	@Cacheable("pokeapi.language")
	Mono<Language> language(@PathVariable String nameOrId);

	@GetExchange("/location/{nameOrId}")
	@Cacheable("pokeapi.location")
	Mono<Location> location(@PathVariable String nameOrId);

	@GetExchange("/location-area/{nameOrId}")
	@Cacheable("pokeapi.location-area")
	Mono<LocationArea> locationArea(@PathVariable String nameOrId);

	@GetExchange("/machine/{id}")
	@Cacheable("pokeapi.machine")
	Mono<Machine> machine(@PathVariable int id);

	@GetExchange("/move/{nameOrId}")
	@Cacheable("pokeapi.move")
	Mono<Move> move(@PathVariable String nameOrId);

	@GetExchange("/move-ailment/{nameOrId}")
	@Cacheable("pokeapi.move-ailment")
	Mono<MoveAilment> moveAilment(@PathVariable String nameOrId);

	@GetExchange("/move-battle-style/{nameOrId}")
	@Cacheable("pokeapi.move-battle-style")
	Mono<MoveBattleStyle> moveBattleStyle(@PathVariable String nameOrId);

	@GetExchange("/move-category/{nameOrId}")
	@Cacheable("pokeapi.move-category")
	Mono<MoveCategory> moveCategory(@PathVariable String nameOrId);

	@GetExchange("/move-damage-class/{nameOrId}")
	@Cacheable("pokeapi.move-damage-class")
	Mono<MoveDamageClass> moveDamageClass(@PathVariable String nameOrId);

	@GetExchange("/move-learn-method/{nameOrId}")
	@Cacheable("pokeapi.move-learn-method")
	Mono<MoveLearnMethod> moveLearnMethod(@PathVariable String nameOrId);

	@GetExchange("/move-target/{nameOrId}")
	@Cacheable("pokeapi.move-target")
	Mono<MoveTarget> moveTarget(@PathVariable String nameOrId);

	@GetExchange("/nature/{nameOrId}")
	@Cacheable("pokeapi.nature")
	Mono<Nature> nature(@PathVariable String nameOrId);

	@GetExchange("/pal-park-area/{nameOrId}")
	@Cacheable("pokeapi.pal-park-area")
	Mono<PalParkArea> palParkArea(@PathVariable String nameOrId);

	@GetExchange("/pokeathlon-stat/{nameOrId}")
	@Cacheable("pokeapi.pokeathlon-stat")
	Mono<PokeathlonStat> pokeathlonStat(@PathVariable String nameOrId);

	@GetExchange("/pokedex/{nameOrId}")
	@Cacheable("pokeapi.pokedex")
	Mono<Pokedex> pokedex(@PathVariable String nameOrId);

	@GetExchange("/pokemon/{nameOrId}")
	@Cacheable("pokeapi.pokemon")
	Mono<Pokemon> pokemon(@PathVariable String nameOrId);

	@GetExchange("/pokemon-color/{nameOrId}")
	@Cacheable("pokeapi.pokemon-color")
	Mono<PokemonColor> pokemonColor(@PathVariable String nameOrId);

	@GetExchange("/pokemon-form/{nameOrId}")
	@Cacheable("pokeapi.pokemon-form")
	Mono<PokemonForm> pokemonForm(@PathVariable String nameOrId);

	@GetExchange("/pokemon-habitat/{nameOrId}")
	@Cacheable("pokeapi.pokemon-habitat")
	Mono<PokemonHabitat> pokemonHabitat(@PathVariable String nameOrId);

	@GetExchange("/pokemon-shape/{nameOrId}")
	@Cacheable("pokeapi.pokemon-shape")
	Mono<PokemonShape> pokemonShape(@PathVariable String nameOrId);

	@GetExchange("/pokemon-species/{nameOrId}")
	@Cacheable("pokeapi.pokemon-species")
	Mono<PokemonSpecies> pokemonSpecies(@PathVariable String nameOrId);

	@GetExchange("/region/{nameOrId}")
	@Cacheable("pokeapi.region")
	Mono<Region> region(@PathVariable String nameOrId);

	@GetExchange("/stat/{nameOrId}")
	@Cacheable("pokeapi.stat")
	Mono<Stat> stat(@PathVariable String nameOrId);

	@GetExchange("/super-contest-effect/{id}")
	@Cacheable("pokeapi.super-contest-effect")
	Mono<SuperContestEffect> superContestEffect(@PathVariable int id);

	@GetExchange("/type/{nameOrId}")
	@Cacheable("pokeapi.type")
	Mono<Type> type(@PathVariable String nameOrId);

	@GetExchange("/version/{nameOrId}")
	@Cacheable("pokeapi.version")
	Mono<Version> version(@PathVariable String nameOrId);

	@GetExchange("/version-group/{nameOrId}")
	@Cacheable("pokeapi.version-group")
	Mono<VersionGroup> versionGroup(@PathVariable String nameOrId);

}
