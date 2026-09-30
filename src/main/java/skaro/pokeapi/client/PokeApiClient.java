package skaro.pokeapi.client;

import java.util.List;
import java.util.function.Supplier;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import skaro.pokeapi.query.PageQuery;
import skaro.pokeapi.resource.NamedApiResource;
import skaro.pokeapi.resource.NamedApiResourceList;
import skaro.pokeapi.resource.PokeApiResource;

/**
 * @deprecated Replaced by {@link com.betwarrior.pokeapi.PokeApi}, a declarative client with immutable records,
 *             typed errors and Spring caching. See {@code docs/adr/0002-pokeapi-client-v2.md}. This library will be
 *             removed in the next release.
 */
@Deprecated(since = "2.0", forRemoval = true)
public interface PokeApiClient {
	
	<T extends PokeApiResource> Mono<NamedApiResourceList<T>> getResource(Class<T> cls);
	<T extends PokeApiResource> Mono<T> getResource(Class<T> cls, String idOrName);
	<T extends PokeApiResource> Mono<NamedApiResourceList<T>> getResource(Class<T> cls, PageQuery query);
	<T extends PokeApiResource> Mono<T> followResource(Supplier<NamedApiResource<T>> resourceSupplier, Class<T> cls);
	<T extends PokeApiResource> Flux<T> followResources(Supplier<List<NamedApiResource<T>>> resourcesSupplier, Class<T> cls);
}
