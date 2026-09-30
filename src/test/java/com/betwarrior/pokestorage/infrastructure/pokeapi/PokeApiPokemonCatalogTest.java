package com.betwarrior.pokestorage.infrastructure.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.application.exception.CatalogUnavailableException;
import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.domain.species.Species;
import com.betwarrior.pokestorage.domain.species.SpeciesAbility;
import com.betwarrior.pokestorage.domain.stats.StatValues;
import com.betwarrior.pokestorage.testsupport.PokeApiStub;

import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;
import skaro.pokeapi.PokeApiConfigurationProperties;
import skaro.pokeapi.PokeApiReactorBaseConfiguration;
import skaro.pokeapi.PokeApiReactorEndpointConfiguration;
import skaro.pokeapi.client.ReactiveNonCachingPokeApiClient;
import skaro.pokeapi.client.WebClientEntityFactory;

class PokeApiPokemonCatalogTest {

	private final PokeApiStub pokeApi = new PokeApiStub();
	private final PokeApiPokemonCatalog catalog = new PokeApiPokemonCatalog(libraryClient(), Duration.ofMillis(1));

	@AfterEach
	void stopPokeApi() throws Exception {
		pokeApi.close();
	}

	@Test
	void givenAnExistingSpecies_whenLookingItUp_thenItsDefaultVarietyIsTranslatedIntoTheDomain() {
		Species pikachu = catalog.findSpecies("pikachu").block();

		assertThat(pikachu.ref().id()).isEqualTo(25);
		assertThat(pikachu.abilities()).containsExactly(
				new SpeciesAbility("static", 1, false), new SpeciesAbility("lightning-rod", 3, true));
		assertThat(pikachu.learnableMoves()).contains("thunderbolt", "quick-attack");
		assertThat(pikachu.baseStats()).isEqualTo(new StatValues(35, 55, 40, 50, 50, 90));
		assertThat(pikachu.types()).containsExactly("electric");
		assertThat(pikachu.genderRatio().femaleEighths()).isEqualTo(4);
		assertThat(pikachu.evolvesFrom()).contains("pichu");
	}

	@Test
	void givenABaseSpecies_whenLookingItUp_thenItEvolvesFromNothing() {
		Species eevee = catalog.findSpecies("eevee").block();

		assertThat(eevee.evolvesFrom()).isEmpty();
	}

	@Test
	void givenADualTypeSpecies_whenLookingItUp_thenTypesKeepPokeApiSlotOrder() {
		Species gyarados = catalog.findSpecies("gyarados").block();

		assertThat(gyarados.types()).containsExactly("water", "flying");
	}

	@Test
	void givenASpeciesUnknownToPokeApi_whenLookingItUp_thenItIsReportedAsUnknown() {
		StepVerifier.create(catalog.findSpecies("missingno"))
				.expectError(UnknownCatalogEntryException.class)
				.verify();
	}

	@Test
	void givenPokeApiFailing_whenLookingUpASpecies_thenItIsRetriedAndReportedAsUnavailable() {
		pokeApi.failWith("pokemon-species/pikachu", 503);

		StepVerifier.create(catalog.findSpecies("pikachu"))
				.expectError(CatalogUnavailableException.class)
				.verify();
		assertThat(pokeApi.requestsTo("pokemon-species/pikachu")).isEqualTo(3);
	}

	@Test
	void givenASpeciesUnknownToPokeApi_whenLookingItUp_thenItIsNotRetried() {
		catalog.findSpecies("missingno").onErrorComplete().block();

		assertThat(pokeApi.requestsTo("pokemon-species/missingno")).isEqualTo(1);
	}

	@Test
	void givenAPokeball_whenLookingItUp_thenItIsRecognizedAsSuch() {
		PokemonCatalog.Item pokeball = catalog.findItem("poke-ball").block();

		assertThat(pokeball.isPokeball()).isTrue();
	}

	@Test
	void givenAHealingItem_whenLookingItUp_thenItIsNotAPokeball() {
		PokemonCatalog.Item potion = catalog.findItem("potion").block();

		assertThat(potion.isPokeball()).isFalse();
	}

	private ReactiveNonCachingPokeApiClient libraryClient() {
		PokeApiReactorBaseConfiguration configuration = new PokeApiReactorBaseConfiguration();
		PokeApiConfigurationProperties properties = new PokeApiConfigurationProperties();
		properties.setBaseUri(URI.create(pokeApi.baseUrl()));
		var webClient = configuration.webClient(HttpClient.create(), configuration.jsonEncoder(),
				configuration.jsonDecoder(), properties);
		var registry = new PokeApiReactorEndpointConfiguration().endpointRegistry();
		return new ReactiveNonCachingPokeApiClient(new WebClientEntityFactory(webClient, registry));
	}

}
