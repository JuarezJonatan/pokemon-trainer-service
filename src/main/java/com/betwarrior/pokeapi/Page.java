package com.betwarrior.pokeapi;

import java.net.URI;
import java.util.List;

import com.betwarrior.pokeapi.model.Lists;

/**
 * One page of an endpoint's listing: links to the resources, plus the total count.
 *
 * @param <R> the link type, {@code NamedRef} or {@code ApiRef}
 */
public record Page<R>(int count, URI next, URI previous, List<R> results) {

	public Page {
		results = Lists.nullSafeCopy(results);
	}

	public boolean hasNext() {
		return next != null;
	}

}
