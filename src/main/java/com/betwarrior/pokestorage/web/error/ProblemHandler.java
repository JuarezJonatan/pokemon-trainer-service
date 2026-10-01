package com.betwarrior.pokestorage.web.error;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.betwarrior.pokestorage.application.exception.CatalogUnavailableException;
import com.betwarrior.pokestorage.application.exception.InvalidItemException;
import com.betwarrior.pokestorage.application.exception.PokemonNotFoundException;
import com.betwarrior.pokestorage.application.exception.ConcurrentUpdateException;
import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.domain.exception.EvolutionNotAllowedException;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.exception.PokemonNotInTeamException;
import com.betwarrior.pokestorage.domain.exception.SpeciesRuleViolationException;
import com.betwarrior.pokestorage.domain.exception.StorageFullException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Maps domain and application failures to RFC 7807 problem details. Every problem carries a stable
 * {@code code} that clients can rely on instead of parsing messages, including the ones Spring produces
 * before a request reaches a controller: a malformed request is {@code malformed-request} and lists in
 * {@code violations} which field or parameter is wrong.
 */
@Slf4j
@RestControllerAdvice
public class ProblemHandler extends ResponseEntityExceptionHandler {

	private static final String TYPE_PREFIX = "urn:pokestorage:problem:";
	private static final String MALFORMED_REQUEST = "malformed-request";

	@ExceptionHandler(InvalidValueException.class)
	ProblemDetail invalidValue(InvalidValueException error) {
		return problem(HttpStatus.BAD_REQUEST, "invalid-value", error.getMessage());
	}

	@ExceptionHandler({ TrainerNotFoundException.class, PokemonNotFoundException.class })
	ProblemDetail notFound(RuntimeException error) {
		return problem(HttpStatus.NOT_FOUND, "not-found", error.getMessage());
	}

	@ExceptionHandler(StorageFullException.class)
	ProblemDetail storageFull(StorageFullException error) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "storage-full", error.getMessage());
		if (error.area() != null) {
			problem.setProperty("area", error.area());
		}
		return problem;
	}

	@ExceptionHandler(PokemonNotInTeamException.class)
	ProblemDetail notInTeam(PokemonNotInTeamException error) {
		return problem(HttpStatus.CONFLICT, "pokemon-not-in-team", error.getMessage());
	}

	@ExceptionHandler(ConcurrentUpdateException.class)
	ProblemDetail concurrentModification(ConcurrentUpdateException error) {
		return problem(HttpStatus.CONFLICT, "concurrent-modification",
				"The Pokemon or the storage kept changing concurrently, please retry");
	}

	@ExceptionHandler(SpeciesRuleViolationException.class)
	ProblemDetail speciesRuleViolation(SpeciesRuleViolationException error) {
		ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_ENTITY, "species-rule-violation",
				"The Pokemon does not comply with its species data in PokeAPI");
		problem.setProperty("violations", error.violations());
		return problem;
	}

	@ExceptionHandler(UnknownCatalogEntryException.class)
	ProblemDetail unknownCatalogEntry(UnknownCatalogEntryException error) {
		return problem(HttpStatus.UNPROCESSABLE_ENTITY, "unknown-pokeapi-entry", error.getMessage());
	}

	@ExceptionHandler(InvalidItemException.class)
	ProblemDetail invalidItem(InvalidItemException error) {
		return problem(HttpStatus.UNPROCESSABLE_ENTITY, "invalid-item", error.getMessage());
	}

	@ExceptionHandler(EvolutionNotAllowedException.class)
	ProblemDetail evolutionNotAllowed(EvolutionNotAllowedException error) {
		return problem(HttpStatus.UNPROCESSABLE_ENTITY, "evolution-not-allowed", error.getMessage());
	}

	@ExceptionHandler(CatalogUnavailableException.class)
	ProblemDetail catalogUnavailable(CatalogUnavailableException error) {
		log.warn("PokeAPI unavailable: {}", error.getMessage(), error.getCause());
		return problem(HttpStatus.SERVICE_UNAVAILABLE, "pokeapi-unavailable",
				"PokeAPI is not available right now, please retry later");
	}

	@Override
	protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(WebExchangeBindException error,
			HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		List<String> violations = Stream.concat(
						error.getFieldErrors().stream().map(field -> field.getField() + ": " + field.getDefaultMessage()),
						error.getGlobalErrors().stream().map(global -> global.getDefaultMessage()))
				.sorted()
				.toList();
		return malformedRequest(error, violations, headers, status, exchange);
	}

	@Override
	protected Mono<ResponseEntity<Object>> handleServerWebInputException(ServerWebInputException error,
			HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		return malformedRequest(error, List.of(describe(error)), headers, status, exchange);
	}

	/**
	 * Gives a {@code code} to every other problem Spring produces (unknown route, wrong method or media type).
	 */
	@Override
	protected Mono<ResponseEntity<Object>> handleExceptionInternal(Exception error, @Nullable Object body,
			@Nullable HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		if (body == null && error instanceof ErrorResponse errorResponse) {
			body = errorResponse.getBody();
		}
		if (body instanceof ProblemDetail problem && problem.getProperties() == null) {
			String code = status.value() == HttpStatus.BAD_REQUEST.value() ? MALFORMED_REQUEST : codeOf(status);
			problem.setType(URI.create(TYPE_PREFIX + code));
			problem.setProperty("code", code);
		}
		return super.handleExceptionInternal(error, body, headers, status, exchange);
	}

	private Mono<ResponseEntity<Object>> malformedRequest(ServerWebInputException error, List<String> violations,
			HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, MALFORMED_REQUEST,
				"The request is malformed: " + String.join("; ", violations));
		problem.setProperty("violations", violations);
		return handleExceptionInternal(error, problem, headers, status, exchange);
	}

	private static String describe(ServerWebInputException error) {
		MethodParameter parameter = error.getMethodParameter();
		Throwable cause = NestedExceptionUtils.getMostSpecificCause(error);
		if (cause instanceof InvalidFormatException invalid) {
			return pathOf(invalid) + ": invalid value '" + invalid.getValue() + "'" + allowedValues(invalid);
		}
		if (cause instanceof JsonMappingException mapping && !mapping.getPath().isEmpty()) {
			return pathOf(mapping) + ": invalid value";
		}
		if (cause instanceof JsonProcessingException) {
			return "body: malformed JSON";
		}
		if (parameter != null && parameter.getParameterName() != null && !parameter.hasParameterAnnotation(RequestBody.class)) {
			return parameter.getParameterName() + ": " + Objects.requireNonNullElse(error.getReason(), "invalid value");
		}
		return "body: " + Objects.requireNonNullElse(error.getReason(), "unreadable request body");
	}

	private static String pathOf(JsonMappingException error) {
		return error.getPath().stream()
				.map(reference -> reference.getFieldName() != null ? reference.getFieldName() : "[" + reference.getIndex() + "]")
				.collect(Collectors.joining(".")).replace(".[", "[");
	}

	private static String allowedValues(InvalidFormatException error) {
		Class<?> type = error.getTargetType();
		if (type == null || !type.isEnum()) {
			return "";
		}
		return " (allowed: " + Stream.of(type.getEnumConstants()).map(Object::toString).toList() + ")";
	}

	private static String codeOf(HttpStatusCode status) {
		HttpStatus known = HttpStatus.resolve(status.value());
		return known == null ? "http-" + status.value() : known.name().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	private static ProblemDetail problem(HttpStatus status, String code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create(TYPE_PREFIX + code));
		problem.setProperty("code", code);
		return problem;
	}

}
