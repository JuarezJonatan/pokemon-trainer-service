package com.betwarrior.pokeapi.error;

import java.net.URI;

/**
 * PokéAPI answered 404: the id or name does not exist on that endpoint.
 */
public final class ResourceNotFoundException extends PokeApiException {

	public ResourceNotFoundException(URI uri) {
		super("PokéAPI has no resource at " + uri, uri, null);
	}

}
