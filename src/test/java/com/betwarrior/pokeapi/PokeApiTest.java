package com.betwarrior.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.betwarrior.pokeapi.error.PokeApiUnavailableException;
import com.betwarrior.pokeapi.error.ResourceNotFoundException;
import com.betwarrior.pokeapi.error.UnexpectedResponseException;
import com.betwarrior.pokeapi.model.evolution.EvolutionChain;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.ref.NamedRef;

import reactor.test.StepVerifier;

class PokeApiTest {

	private final PokeApiServer server = new PokeApiServer();
	private final ApplicationContextRunner context = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PokeApiAutoConfiguration.class))
			.withPropertyValues("pokeapi.base-url=" + server.baseUrl(), "pokeapi.retry.first-backoff=1ms");

	@AfterEach
	void stopServer() throws IOException {
		server.close();
	}

	@Test
	void givenAPokemonName_whenFetchingIt_thenItIsRequestedByNameAndMapped() {
		withPokeApi(pokeApi -> {
			Pokemon pikachu = pokeApi.pokemon("pikachu").block();

			assertThat(pikachu.id()).isEqualTo(25);
			assertThat(pikachu.name()).isEqualTo("pikachu");
			assertThat(server.requests()).containsExactly("pokemon/pikachu");
		});
	}

	@Test
	void givenAResourceWithoutName_whenFetchingItById_thenItIsMapped() {
		withPokeApi(pokeApi -> {
			EvolutionChain chain = pokeApi.evolutionChain(1).block();

			assertThat(chain.chain().species().name()).isEqualTo("bulbasaur");
			assertThat(server.requests()).containsExactly("evolution-chain/1");
		});
	}

	@Test
	void givenAnEndpoint_whenListingAPage_thenLinksAndPaginationAreMapped() {
		withPokeApi(pokeApi -> {
			Page<NamedRef<Object>> page = pokeApi.list("pokemon", 0, 3).block();

			assertThat(page.count()).isEqualTo(1351);
			assertThat(page.hasNext()).isTrue();
			assertThat(page.results()).extracting(NamedRef::name).containsExactly("bulbasaur", "ivysaur", "venusaur");
			assertThat(page.results()).extracting(NamedRef::id).containsExactly(1, 2, 3);
			assertThat(server.requests()).containsExactly("pokemon?offset=0&limit=3");
		});
	}

	@Test
	void givenAnEndpointWithoutNames_whenListingAPage_thenLinksHaveNoName() {
		withPokeApi(pokeApi -> {
			Page<NamedRef<Object>> page = pokeApi.list("evolution-chain", 0, 2).block();

			assertThat(page.results()).extracting(NamedRef::name).containsOnlyNulls();
			assertThat(page.results()).extracting(NamedRef::id).containsExactly(1, 2);
		});
	}

	@Test
	void givenAnUnknownName_whenFetchingIt_thenItIsReportedAsNotFoundWithoutRetrying() {
		server.respondWith("pokemon/missingno", 404);

		withPokeApi(pokeApi -> {
			StepVerifier.create(pokeApi.pokemon("missingno"))
					.expectError(ResourceNotFoundException.class)
					.verify();

			assertThat(server.requestsTo("pokemon/missingno")).isEqualTo(1);
		});
	}

	@Test
	void givenPokeApiFailingBriefly_whenFetching_thenTheRequestIsRetriedUntilItSucceeds() {
		server.respondWith("pokemon/pikachu", 503, 500);

		withPokeApi(pokeApi -> {
			Pokemon pikachu = pokeApi.pokemon("pikachu").block();

			assertThat(pikachu.name()).isEqualTo("pikachu");
			assertThat(server.requestsTo("pokemon/pikachu")).isEqualTo(3);
		});
	}

	@Test
	void givenPokeApiFailingPersistently_whenFetching_thenItIsReportedAsUnavailableAfterRetrying() {
		server.respondWith("pokemon/pikachu", 503, 503, 503);

		withPokeApi(pokeApi -> {
			StepVerifier.create(pokeApi.pokemon("pikachu"))
					.expectError(PokeApiUnavailableException.class)
					.verify();

			assertThat(server.requestsTo("pokemon/pikachu")).isEqualTo(3);
		});
	}

	@Test
	void givenPokeApiRateLimiting_whenFetching_thenTheRequestIsRetried() {
		server.respondWith("pokemon/pikachu", 429);

		withPokeApi(pokeApi -> {
			Pokemon pikachu = pokeApi.pokemon("pikachu").block();

			assertThat(pikachu.name()).isEqualTo("pikachu");
		});
	}

	@Test
	void givenABadRequest_whenFetching_thenItIsReportedAsUnexpectedWithoutRetrying() {
		server.respondWith("pokemon/pikachu", 400);

		withPokeApi(pokeApi -> {
			StepVerifier.create(pokeApi.pokemon("pikachu"))
					.expectError(UnexpectedResponseException.class)
					.verify();

			assertThat(server.requestsTo("pokemon/pikachu")).isEqualTo(1);
		});
	}

	@Test
	void givenAnUnreachablePokeApi_whenFetching_thenItIsReportedAsUnavailable() throws IOException {
		server.close();

		withPokeApi(pokeApi -> StepVerifier.create(pokeApi.pokemon("pikachu"))
				.expectError(PokeApiUnavailableException.class)
				.verify());
	}

	private void withPokeApi(Consumer<PokeApi> test) {
		context.run(started -> test.accept(started.getBean(PokeApi.class)));
	}

}
