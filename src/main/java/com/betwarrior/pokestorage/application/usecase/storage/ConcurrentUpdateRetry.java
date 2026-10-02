package com.betwarrior.pokestorage.application.usecase.storage;

import java.time.Duration;

import com.betwarrior.pokestorage.application.exception.ConcurrentUpdateException;
import com.betwarrior.pokestorage.domain.storage.StorageCapacity;

import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

/**
 * Retries an operation that lost a race: two operations picked the same free slot (the database unique
 * constraint rejects one), or the Pokemon changed after it was read (its version no longer matches).
 * <p>
 * Every lost race means another operation committed a change this one had not seen, so a capture can lose at
 * most once per slot. Allowing as many retries as there are slots lets every capture that fits get in, and the
 * jittered backoff keeps contenders from colliding again on the same slot.
 */
public final class ConcurrentUpdateRetry {

	private static final Duration FIRST_BACKOFF = Duration.ofMillis(5);
	private static final Duration MAX_BACKOFF = Duration.ofMillis(100);

	private ConcurrentUpdateRetry() {
	}

	public static RetryBackoffSpec boundedBy(StorageCapacity capacity) {
		return Retry.backoff(capacity.team() + capacity.box(), FIRST_BACKOFF)
				.maxBackoff(MAX_BACKOFF)
				.jitter(0.5)
				.filter(ConcurrentUpdateException.class::isInstance)
				.onRetryExhaustedThrow((spec, signal) -> signal.failure());
	}

}
