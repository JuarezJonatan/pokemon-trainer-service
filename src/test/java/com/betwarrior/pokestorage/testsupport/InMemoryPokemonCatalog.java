package com.betwarrior.pokestorage.testsupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.application.port.PokemonCatalog;
import com.betwarrior.pokestorage.domain.species.Species;

import reactor.core.publisher.Mono;

public class InMemoryPokemonCatalog implements PokemonCatalog {

	private final Map<String, Species> species = new ConcurrentHashMap<>();
	private final Map<String, Item> items = new ConcurrentHashMap<>();
	private final AtomicInteger lookups = new AtomicInteger();

	public InMemoryPokemonCatalog with(Species... entries) {
		for (Species entry : entries) {
			species.put(entry.name(), entry);
		}
		return this;
	}

	public InMemoryPokemonCatalog withItem(String name, String category) {
		items.put(name, new Item(name, category));
		return this;
	}

	public int lookups() {
		return lookups.get();
	}

	@Override
	public Mono<Species> findSpecies(String name) {
		return Mono.defer(() -> {
			lookups.incrementAndGet();
			return Mono.justOrEmpty(species.get(name))
					.switchIfEmpty(Mono.error(() -> new UnknownCatalogEntryException("species", name)));
		});
	}

	@Override
	public Mono<Item> findItem(String name) {
		return Mono.defer(() -> {
			lookups.incrementAndGet();
			return Mono.justOrEmpty(items.get(name))
					.switchIfEmpty(Mono.error(() -> new UnknownCatalogEntryException("item", name)));
		});
	}

}
