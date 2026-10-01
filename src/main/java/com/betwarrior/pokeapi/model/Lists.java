package com.betwarrior.pokeapi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PokéAPI omits or nulls empty lists and some lists hold {@code null} entries, so records copy them through here
 * instead of {@link List#copyOf}, which rejects nulls.
 */
public final class Lists {

	private Lists() {
	}

	public static <E> List<E> nullSafeCopy(List<E> list) {
		return list == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(list));
	}

}
