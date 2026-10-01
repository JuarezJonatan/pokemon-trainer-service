package com.betwarrior.pokeapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.betwarrior.pokeapi.error.PokeApiUnavailableException;
import com.betwarrior.pokeapi.error.ResourceNotFoundException;
import com.betwarrior.pokeapi.error.UnexpectedResponseException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Turns PokéAPI responses into the client's error model and retries transient failures, so callers only ever see
 * a {@link com.betwarrior.pokeapi.error.PokeApiException}.
 */
@RequiredArgsConstructor
class PokeApiErrorFilter implements ExchangeFilterFunction {

	private final PokeApiProperties.Retry retry;

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(request)
				.flatMap(response -> checkStatus(request, response))
				.retryWhen(Retry.backoff(retry.maxRetries(), retry.firstBackoff())
						.filter(PokeApiErrorFilter::isTransient)
						.onRetryExhaustedThrow((spec, signal) ->
								new PokeApiUnavailableException(request.url(), signal.failure())));
	}

	private static Mono<ClientResponse> checkStatus(ClientRequest request, ClientResponse response) {
		if (response.statusCode().is2xxSuccessful()) {
			return Mono.just(response);
		}
		return response.releaseBody().then(Mono.error(() -> failureFor(request, response)));
	}

	private static RuntimeException failureFor(ClientRequest request, ClientResponse response) {
		if (response.statusCode().value() == HttpStatus.NOT_FOUND.value()) {
			return new ResourceNotFoundException(request.url());
		}
		if (response.statusCode().is5xxServerError()
				|| response.statusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
			return new TransientStatusException(response.statusCode().value());
		}
		return new UnexpectedResponseException("status " + response.statusCode().value(), request.url(), null);
	}

	private static boolean isTransient(Throwable error) {
		return error instanceof TransientStatusException || error instanceof WebClientRequestException;
	}

	private static final class TransientStatusException extends RuntimeException {

		TransientStatusException(int status) {
			super("PokéAPI answered " + status);
		}

	}

}
