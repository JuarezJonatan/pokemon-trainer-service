package com.betwarrior.pokeapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * The JSON mapping PokéAPI needs: snake_case fields, and unknown fields ignored so new PokéAPI fields never break
 * the client. Kept separate from the application's {@code ObjectMapper}.
 */
public final class PokeApiJson {

	private PokeApiJson() {
	}

	public static JsonMapper mapper() {
		return JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.build();
	}

}
