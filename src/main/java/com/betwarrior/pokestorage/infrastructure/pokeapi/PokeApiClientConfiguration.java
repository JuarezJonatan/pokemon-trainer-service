package com.betwarrior.pokestorage.infrastructure.pokeapi;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import skaro.pokeapi.PokeApiReactorCachingConfiguration;

@Configuration
@EnableCaching
@Import(PokeApiReactorCachingConfiguration.class)
public class PokeApiClientConfiguration {

	@ConfigurationProperties("pokestorage.pokeapi")
	public record PokeApiTimeouts(Duration connectTimeout, Duration responseTimeout) {
	}

	@Bean
	public ConnectionProvider pokeApiConnectionProvider() {
		return ConnectionProvider.builder("pokeapi")
				.maxConnections(100)
				.maxIdleTime(Duration.ofSeconds(20))
				.pendingAcquireTimeout(Duration.ofSeconds(10))
				.build();
	}

	@Bean
	public HttpClient pokeApiHttpClient(ConnectionProvider pokeApiConnectionProvider, PokeApiTimeouts timeouts) {
		return HttpClient.create(pokeApiConnectionProvider)
				.compress(true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeouts.connectTimeout().toMillis())
				.responseTimeout(timeouts.responseTimeout())
				.followRedirect(true);
	}

}
