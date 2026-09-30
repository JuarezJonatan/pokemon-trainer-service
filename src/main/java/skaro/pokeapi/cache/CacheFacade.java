package skaro.pokeapi.cache;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import skaro.pokeapi.resource.PokeApiResource;

/**
 * @deprecated Replaced by {@link com.betwarrior.pokeapi.PokeApi}, a declarative client with immutable records,
 *             typed errors and Spring caching. See {@code docs/adr/0002-pokeapi-client-v2.md}. This library will be
 *             removed in the next release.
 */
@Deprecated(since = "2.0", forRemoval = true)
public interface CacheFacade {

	<T extends PokeApiResource> Mono<T> get(CacheSpec<T> cacheSpec);
	<T extends PokeApiResource> Flux<T> getMany(List<CacheSpec<T>> cacheSpecs);
	
}
