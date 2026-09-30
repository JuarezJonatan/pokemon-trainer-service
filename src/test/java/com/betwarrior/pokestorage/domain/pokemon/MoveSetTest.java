package com.betwarrior.pokestorage.domain.pokemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;

class MoveSetTest {

	@Test
	void givenFourDifferentMoves_whenCreatingAMoveSet_thenItIsAccepted() {
		List<String> moves = List.of("thunderbolt", "quick-attack", "iron-tail", "thunder-shock");

		MoveSet moveSet = new MoveSet(moves);

		assertThat(moveSet.moves()).containsExactlyElementsOf(moves);
	}

	@Test
	void givenFiveMoves_whenCreatingAMoveSet_thenItIsRejected() {
		List<String> moves = List.of("a", "b", "c", "d", "e");

		assertThatThrownBy(() -> new MoveSet(moves))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("at most 4");
	}

	@Test
	void givenNoMoves_whenCreatingAMoveSet_thenItIsRejected() {
		assertThatThrownBy(() -> new MoveSet(List.of()))
				.isInstanceOf(InvalidValueException.class);
	}

	@Test
	void givenARepeatedMove_whenCreatingAMoveSet_thenItIsRejected() {
		List<String> moves = List.of("tackle", "tackle");

		assertThatThrownBy(() -> new MoveSet(moves))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("twice");
	}

	@Test
	void givenAMutableList_whenItChangesAfterCreatingTheMoveSet_thenTheMoveSetIsUnaffected() {
		List<String> moves = new ArrayList<>(List.of("tackle"));
		MoveSet moveSet = new MoveSet(moves);

		moves.add("growl");

		assertThat(moveSet.moves()).containsExactly("tackle");
	}

}
