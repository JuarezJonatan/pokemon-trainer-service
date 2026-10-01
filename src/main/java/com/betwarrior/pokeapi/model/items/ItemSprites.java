package com.betwarrior.pokeapi.model.items;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemSprites(
		@JsonProperty("default") String imageUrl) {

}
