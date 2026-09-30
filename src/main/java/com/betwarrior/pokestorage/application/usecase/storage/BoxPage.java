package com.betwarrior.pokestorage.application.usecase.storage;

import java.util.List;

import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;

public record BoxPage(List<PokemonSpecimen> pokemon, int page, int size, long totalElements) {

	public long totalPages() {
		return (totalElements + size - 1) / size;
	}

}
