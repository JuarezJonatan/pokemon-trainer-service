package skaro.pokeapi;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import skaro.pokeapi.cache.CacheFacade;
import skaro.pokeapi.cache.ReactiveCacheManagerCacheFacade;
import skaro.pokeapi.client.PokeApiClient;
import skaro.pokeapi.client.PokeApiEntityFactory;
import skaro.pokeapi.client.ReactiveCachingPokeApiClient;

/**
 * @deprecated Replaced by {@link com.betwarrior.pokeapi.PokeApi}, a declarative client with immutable records,
 *             typed errors and Spring caching. See {@code docs/adr/0002-pokeapi-client-v2.md}. This library will be
 *             removed in the next release.
 */
@Deprecated(since = "2.0", forRemoval = true)
@Configuration
@Import(PokeApiReactorBaseConfiguration.class)
public class PokeApiReactorCachingConfiguration {
	public static final String CACHE_FACADE_BEAN = "pokeApiReactorCacheFacade";
	
	@Bean(CACHE_FACADE_BEAN)
	public CacheFacade cacheFacade(CacheManager cacheManager) {
		return new ReactiveCacheManagerCacheFacade(cacheManager);
	}
	
	@Bean
	public PokeApiClient pokeApiClient(PokeApiEntityFactory entityFactory, CacheFacade cacheFacade) {
		return new ReactiveCachingPokeApiClient(entityFactory, cacheFacade);
	}
	
}
