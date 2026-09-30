package com.betwarrior.pokestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PokemonTrainerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokemonTrainerServiceApplication.class, args);
	}

}
