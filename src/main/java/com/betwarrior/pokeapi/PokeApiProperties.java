package com.betwarrior.pokeapi;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

/**
 * Settings under {@code pokeapi.*}. Every one has a default, so the client works with no configuration at all.
 *
 * @param baseUrl         PokéAPI root, e.g. a mirror or a stub in tests
 * @param connectTimeout  time allowed to open a connection
 * @param responseTimeout time allowed between request and response
 * @param maxResponseSize largest body accepted; some Pokémon (e.g. {@code mew}) exceed 600 KB
 * @param retry           retries on transient failures (5xx, 429, timeouts, connection errors)
 */
@ConfigurationProperties("pokeapi")
public record PokeApiProperties(
		@DefaultValue("https://pokeapi.co/api/v2") URI baseUrl,
		@DefaultValue("2s") Duration connectTimeout,
		@DefaultValue("5s") Duration responseTimeout,
		@DefaultValue("10MB") DataSize maxResponseSize,
		@DefaultValue Retry retry) {

	public record Retry(
			@DefaultValue("2") int maxRetries,
			@DefaultValue("200ms") Duration firstBackoff) {
	}

}
