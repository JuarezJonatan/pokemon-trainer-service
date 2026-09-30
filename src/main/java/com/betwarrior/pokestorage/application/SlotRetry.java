package com.betwarrior.pokestorage.application;

import reactor.util.retry.Retry;
import reactor.util.retry.RetrySpec;

/**
 * Two concurrent operations may pick the same free slot; the database unique constraint rejects one
 * of them and it is retried reading the storage again.
 */
final class SlotRetry {

	private SlotRetry() {
	}

	static RetrySpec onConcurrentSlotAssignment() {
		return Retry.max(3)
				.filter(SlotAlreadyTakenException.class::isInstance)
				.onRetryExhaustedThrow((spec, signal) -> signal.failure());
	}

}
