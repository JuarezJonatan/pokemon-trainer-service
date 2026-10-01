package com.betwarrior.pokeapi.ref;

import java.net.URI;

public record ApiRef<T>(URI url) implements Ref<T> {

}
