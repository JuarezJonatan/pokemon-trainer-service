package com.betwarrior.pokestorage.infrastructure.pokeapi;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * PokeAPI data is static, so the client's responses are cached (Caffeine, see {@code spring.cache.*}).
 */
@Configuration
@EnableCaching
public class PokeApiCachingConfiguration {

}
