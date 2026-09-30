package com.betwarrior.pokestorage.application;

import com.betwarrior.pokestorage.domain.Species;

import reactor.core.publisher.Mono;

/**
 * Source of static Pokemon data (species and items, i.e. PokeAPI). Fails with {@link UnknownCatalogEntryException} when an
 * entry does not exist and with {@link CatalogUnavailableException} when the source cannot answer.
 */
public interface PokemonCatalog {

	Mono<Species> findSpecies(String name);

	Mono<Item> findItem(String name);

	record Item(String name, String category) {

		public boolean isPokeball() {
			return category.endsWith("-balls");
		}

	}

}
