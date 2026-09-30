package skaro.pokeapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import skaro.pokeapi.client.PokeApiClient;
import skaro.pokeapi.client.PokeApiEntityFactory;
import skaro.pokeapi.client.ReactiveNonCachingPokeApiClient;

/**
 * @deprecated Replaced by {@link com.betwarrior.pokeapi.PokeApi}, a declarative client with immutable records,
 *             typed errors and Spring caching. See {@code docs/adr/0002-pokeapi-client-v2.md}. This library will be
 *             removed in the next release.
 */
@Deprecated(since = "2.0", forRemoval = true)
@Configuration
@Import(PokeApiReactorBaseConfiguration.class)
public class PokeApiReactorNonCachingConfiguration {

	@Bean
	public PokeApiClient pokeApiClient(PokeApiEntityFactory entityFactory) {
		return new ReactiveNonCachingPokeApiClient(entityFactory);
	}
	
}
