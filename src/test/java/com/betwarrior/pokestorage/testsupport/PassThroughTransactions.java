package com.betwarrior.pokestorage.testsupport;

import org.reactivestreams.Publisher;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PassThroughTransactions implements TransactionalOperator {

	@Override
	public <T> Mono<T> transactional(Mono<T> mono) {
		return mono;
	}

	@Override
	public <T> Flux<T> execute(TransactionCallback<T> action) {
		return Flux.defer(() -> {
			Publisher<T> result = action.doInTransaction((ReactiveTransaction) null);
			return Flux.from(result);
		});
	}

}
