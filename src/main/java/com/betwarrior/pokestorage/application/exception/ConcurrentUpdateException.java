package com.betwarrior.pokestorage.application.exception;

/**
 * A write lost a race against another operation on the same data. It is safe to retry from a fresh read,
 * which {@link com.betwarrior.pokestorage.application.usecase.storage.ConcurrentUpdateRetry} does.
 */
public abstract class ConcurrentUpdateException extends ApplicationException {

	protected ConcurrentUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

}
