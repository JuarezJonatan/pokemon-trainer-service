package com.betwarrior.pokestorage.web.error;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import com.betwarrior.pokestorage.application.exception.CatalogUnavailableException;
import com.betwarrior.pokestorage.application.exception.InvalidItemException;
import com.betwarrior.pokestorage.application.exception.PokemonNotFoundException;
import com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException;
import com.betwarrior.pokestorage.application.exception.TrainerNotFoundException;
import com.betwarrior.pokestorage.application.exception.UnknownCatalogEntryException;
import com.betwarrior.pokestorage.domain.exception.EvolutionNotAllowedException;
import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.exception.PokemonNotInTeamException;
import com.betwarrior.pokestorage.domain.exception.SpeciesRuleViolationException;
import com.betwarrior.pokestorage.domain.exception.StorageFullException;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps domain and application failures to RFC 7807 problem details. Every problem carries a stable
 * {@code code} that clients can rely on instead of parsing messages.
 */
@Slf4j
@RestControllerAdvice
public class ProblemHandler extends ResponseEntityExceptionHandler {

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

	@ExceptionHandler(SlotAlreadyTakenException.class)
	ProblemDetail concurrentModification(SlotAlreadyTakenException error) {
		return problem(HttpStatus.CONFLICT, "concurrent-modification",
				"The storage changed concurrently, please retry");
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

	private static ProblemDetail problem(HttpStatus status, String code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("urn:pokestorage:problem:" + code));
		problem.setProperty("code", code);
		return problem;
	}

}
