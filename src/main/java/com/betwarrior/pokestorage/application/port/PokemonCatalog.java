package com.betwarrior.pokestorage.application.port;

import com.betwarrior.pokestorage.domain.species.Species;

import reactor.core.publisher.Mono;

/**
 * Source of static Pokemon data (species and items, i.e. PokeAPI). Fails with
 * {@link com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException} when an entry does
 * not exist and with {@link com.betwarrior.pokestorage.application.exception.CatalogUnavailableException}
 * when the source cannot answer.
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
