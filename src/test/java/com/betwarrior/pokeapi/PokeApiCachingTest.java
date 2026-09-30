package com.betwarrior.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import com.betwarrior.pokeapi.error.ResourceNotFoundException;

class PokeApiCachingTest {

	private final PokeApiServer server = new PokeApiServer();
	private final ApplicationContextRunner context = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PokeApiAutoConfiguration.class, CacheAutoConfiguration.class))
			.withUserConfiguration(CachingApplication.class)
			.withPropertyValues("pokeapi.base-url=" + server.baseUrl(), "spring.cache.type=caffeine");

	@AfterEach
	void stopServer() throws IOException {
		server.close();
	}

	@Test
	void givenAFetchedPokemon_whenFetchingItAgainWithDifferentCase_thenPokeApiIsCalledOnce() {
		withPokeApi(pokeApi -> {
			pokeApi.pokemon("Pikachu").block();
			pokeApi.pokemon("pikachu").block();

			assertThat(server.requests()).containsExactly("pokemon/Pikachu");
		});
	}

	@Test
	void givenTheSameIdentifierOnTwoEndpoints_whenFetchingBoth_thenEachEndpointIsCachedSeparately() {
		withPokeApi(pokeApi -> {
			pokeApi.pokemon("1").block();
			pokeApi.ability("1").block();

			assertThat(server.requests()).containsExactly("pokemon/1", "ability/1");
		});
	}

	@Test
	void givenListingsOfTwoEndpoints_whenListingBoth_thenEachListingIsCachedSeparately() {
		withPokeApi(pokeApi -> {
			var pokemon = pokeApi.list("pokemon", 0, 3).block();
			var chains = pokeApi.list("evolution-chain", 0, 2).block();

			assertThat(pokemon.results()).hasSize(3);
			assertThat(chains.results()).hasSize(2);
			assertThat(server.requests()).hasSize(2);
		});
	}

	@Test
	void givenAFailedFetch_whenFetchingAgain_thenTheFailureWasNotCached() {
		server.respondWith("pokemon/pikachu", 404);

		withPokeApi(pokeApi -> {
			pokeApi.pokemon("pikachu").onErrorComplete(ResourceNotFoundException.class).block();
			pokeApi.pokemon("pikachu").block();

			assertThat(server.requestsTo("pokemon/pikachu")).isEqualTo(2);
		});
	}

	private void withPokeApi(Consumer<PokeApi> test) {
		context.run(started -> test.accept(started.getBean(PokeApi.class)));
	}

	@Configuration(proxyBeanMethods = false)
	@EnableCaching
	static class CachingApplication {
	}

}
