package com.betwarrior.pokeapi.model;

/**
 * A resource that can also be fetched by its unique, lower-case name.
 */
public interface NamedResource extends Resource {

	String name();

}
