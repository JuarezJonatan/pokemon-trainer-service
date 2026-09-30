package com.betwarrior.pokeapi.error;

import java.net.URI;

/**
 * PokéAPI kept failing with a transient error (5xx, 429, timeout or I/O error) after every retry.
 */
public final class PokeApiUnavailableException extends PokeApiException {

	public PokeApiUnavailableException(URI uri, Throwable cause) {
		super("PokéAPI is unavailable for " + uri, uri, cause);
	}

}
