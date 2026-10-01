package com.betwarrior.pokeapi;

import java.util.Arrays;
import java.util.Locale;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * Provides a ready-to-inject {@link PokeApi}, configured through {@link PokeApiProperties}. It decodes with
 * {@link PokeApiJson} and never touches the application's {@code ObjectMapper}.
 */
@AutoConfiguration(after = WebClientAutoConfiguration.class)
@EnableConfigurationProperties(PokeApiProperties.class)
public class PokeApiAutoConfiguration {

	public static final String KEY_GENERATOR = "pokeApiKeyGenerator";

	@Bean
	@ConditionalOnMissingBean
	public PokeApi pokeApi(ObjectProvider<WebClient.Builder> webClientBuilder, PokeApiProperties properties) {
		return createClient(webClientBuilder.getIfAvailable(WebClient::builder), properties);
	}

	/**
	 * Builds the client without a Spring context, e.g. in tests or plain applications.
	 */
	public static PokeApi createClient(WebClient.Builder webClientBuilder, PokeApiProperties properties) {
		WebClient webClient = webClientBuilder
				.baseUrl(properties.baseUrl().toString())
				.clientConnector(new ReactorClientHttpConnector(httpClient(properties)))
				.codecs(codecs -> codecs.defaultCodecs().jackson2JsonDecoder(jsonDecoder(properties)))
				.filter(new PokeApiErrorFilter(properties.retry()))
				.build();
		return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
				.build()
				.createClient(PokeApi.class);
	}

	/**
	 * PokéAPI treats names case-insensitively, so {@code "Pikachu"} and {@code "pikachu"} share a cache entry.
	 */
	@Bean(KEY_GENERATOR)
	public KeyGenerator pokeApiKeyGenerator() {
		return (target, method, params) -> SimpleKeyGenerator.generateKey(Arrays.stream(params)
				.map(param -> param instanceof String text ? text.toLowerCase(Locale.ROOT) : param)
				.toArray());
	}

	private static HttpClient httpClient(PokeApiProperties properties) {
		return HttpClient.create()
				.compress(true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.connectTimeout().toMillis())
				.responseTimeout(properties.responseTimeout());
	}

	private static Jackson2JsonDecoder jsonDecoder(PokeApiProperties properties) {
		Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(PokeApiJson.mapper());
		decoder.setMaxInMemorySize((int) properties.maxResponseSize().toBytes());
		return decoder;
	}

	/**
	 * {@link PokeApi} returns {@code Mono}, and Spring caches reactive results only in Caffeine's async mode.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(CaffeineCacheManager.class)
	static class ReactiveCaching {

		@Bean
		CacheManagerCustomizer<CaffeineCacheManager> pokeApiAsyncCaches() {
			return cacheManager -> cacheManager.setAsyncCacheMode(true);
		}

	}

}
