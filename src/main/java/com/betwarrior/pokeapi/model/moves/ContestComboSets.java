package com.betwarrior.pokeapi.model.moves;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContestComboSets(
		ContestComboDetail normal,
		@JsonProperty("super") ContestComboDetail superb) {

}
