package com.betwarrior.pokeapi.ref;

import java.net.URI;

/**
 * A link from one PokéAPI resource to another. Fetch the target with the matching {@code PokeApi} method, e.g.
 * {@code pokeApi.pokemon(species.defaultVariety().name())}.
 *
 * @param <T> the type of the linked resource
 */
public sealed interface Ref<T> permits NamedRef, ApiRef {

	URI url();

	/**
	 * The id at the end of the link, e.g. {@code 25} for {@code .../pokemon/25/}.
	 */
	default int id() {
		String[] segments = url().getPath().split("/");
		return Integer.parseInt(segments[segments.length - 1]);
	}

}
