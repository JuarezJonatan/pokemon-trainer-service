package com.betwarrior.pokeapi.error;

import java.net.URI;

/**
 * Every failure the client reports. The hierarchy is sealed, so callers can handle it exhaustively with a
 * {@code switch}.
 */
public abstract sealed class PokeApiException extends RuntimeException
		permits ResourceNotFoundException, PokeApiUnavailableException, UnexpectedResponseException {

	private final URI uri;

	protected PokeApiException(String message, URI uri, Throwable cause) {
		super(message, cause);
		this.uri = uri;
	}

	public URI uri() {
		return uri;
	}

}
