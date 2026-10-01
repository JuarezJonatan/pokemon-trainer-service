package com.betwarrior.pokeapi.error;

import java.net.URI;

/**
 * PokéAPI answered something the client cannot use: an unexpected status or a body that is not the resource.
 * Retrying will not help.
 */
public final class UnexpectedResponseException extends PokeApiException {

	public UnexpectedResponseException(String reason, URI uri, Throwable cause) {
		super("Unexpected PokéAPI response for " + uri + ": " + reason, uri, cause);
	}

}
