package com.betwarrior.pokeapi.model.utility;

import com.betwarrior.pokeapi.model.games.VersionGroup;
import com.betwarrior.pokeapi.model.machines.Machine;
import com.betwarrior.pokeapi.ref.ApiRef;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MachineVersionDetail(
		ApiRef<Machine> machine,
		NamedRef<VersionGroup> versionGroup) {

}
