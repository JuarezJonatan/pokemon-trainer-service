package com.betwarrior.pokeapi.model.utility;

import java.util.List;
import java.util.Optional;

/**
 * A resource whose name is translated into several languages.
 */
public interface Localized {

	List<Name> names();

	default Optional<String> nameIn(String language) {
		return names().stream()
				.filter(name -> name.language().name().equalsIgnoreCase(language))
				.map(Name::name)
				.findFirst();
	}

}
