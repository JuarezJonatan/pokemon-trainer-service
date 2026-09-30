package com.betwarrior.pokestorage.application;

public class UnknownCatalogEntryException extends ApplicationException {

	public UnknownCatalogEntryException(String kind, String name) {
		super("Unknown %s '%s' in PokeAPI".formatted(kind, name));
	}

}
