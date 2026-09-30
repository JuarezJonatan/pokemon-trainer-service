package com.betwarrior.pokestorage.application.usecase.storage;

import com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException;

import reactor.util.retry.Retry;
import reactor.util.retry.RetrySpec;

/**
 * Two concurrent operations may pick the same free slot; the database unique constraint rejects one
 * of them and it is retried reading the storage again.
 */
public final class SlotRetry {

	private SlotRetry() {
	}

	public static RetrySpec onConcurrentSlotAssignment() {
		return Retry.max(3)
				.filter(SlotAlreadyTakenException.class::isInstance)
				.onRetryExhaustedThrow((spec, signal) -> signal.failure());
	}

}
