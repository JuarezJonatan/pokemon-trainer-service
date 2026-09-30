package com.betwarrior.pokestorage.domain.pokemon;

import java.util.HashSet;
import java.util.List;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

public record MoveSet(List<String> moves) {

	public static final int MAX_MOVES = 4;

	public MoveSet {
		if (moves == null || moves.isEmpty()) {
			throw new InvalidValueException("A Pokemon must know at least one move");
		}
		if (moves.size() > MAX_MOVES) {
			throw new InvalidValueException(
					"A Pokemon can know at most %d moves but got %d".formatted(MAX_MOVES, moves.size()));
		}
		if (moves.stream().anyMatch(move -> move == null || move.isBlank())) {
			throw new InvalidValueException("Move names cannot be blank");
		}
		if (new HashSet<>(moves).size() != moves.size()) {
			throw new InvalidValueException("A Pokemon cannot know the same move twice");
		}
		moves = List.copyOf(moves);
	}

}
