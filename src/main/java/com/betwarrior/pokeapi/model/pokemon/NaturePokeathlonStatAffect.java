package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.ref.NamedRef;

public record NaturePokeathlonStatAffect(
		Integer maxChange,
		NamedRef<Nature> nature) {

}
