package com.betwarrior.pokestorage.application;

import java.util.Locale;

/**
 * PokeAPI identifies species, abilities, moves and items by lowercase kebab-case names.
 */
final class CatalogNames {

	private CatalogNames() {
	}

	static String normalize(String name) {
		return name.trim().toLowerCase(Locale.ROOT);
	}

}
