package com.betwarrior.pokeapi.ref;

import java.net.URI;

public record NamedRef<T>(String name, URI url) implements Ref<T> {

}
