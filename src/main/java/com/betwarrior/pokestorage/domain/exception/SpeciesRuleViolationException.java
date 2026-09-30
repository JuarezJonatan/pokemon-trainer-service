package com.betwarrior.pokestorage.domain.exception;

import java.util.List;

public class SpeciesRuleViolationException extends DomainException {

	private final List<String> violations;

	public SpeciesRuleViolationException(List<String> violations) {
		super(String.join("; ", violations));
		this.violations = List.copyOf(violations);
	}

	public List<String> violations() {
		return violations;
	}

}
