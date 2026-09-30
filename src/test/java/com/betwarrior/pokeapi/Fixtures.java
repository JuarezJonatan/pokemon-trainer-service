package com.betwarrior.pokeapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class Fixtures {

	private Fixtures() {
	}

	public static <T> T read(String endpoint, Class<T> type) {
		try (InputStream json = Fixtures.class.getResourceAsStream("/pokeapi-v2/" + endpoint + ".json")) {
			return PokeApiJson.mapper().readValue(json, type);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
