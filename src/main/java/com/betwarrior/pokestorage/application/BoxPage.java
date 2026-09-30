package com.betwarrior.pokestorage.application;

import java.util.List;

import com.betwarrior.pokestorage.domain.PokemonSpecimen;

public record BoxPage(List<PokemonSpecimen> pokemon, int page, int size, long totalElements) {

	public long totalPages() {
		return (totalElements + size - 1) / size;
	}

}
