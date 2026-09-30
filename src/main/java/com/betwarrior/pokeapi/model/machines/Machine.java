package com.betwarrior.pokeapi.model.machines;

import com.betwarrior.pokeapi.model.Resource;
import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.model.moves.Move;
import com.betwarrior.pokeapi.ref.NamedRef;

public record Machine(
		int id,
		NamedRef<Item> item,
		NamedRef<Move> move,
		NamedRef<VersionGroup> versionGroup) implements Resource {

}
