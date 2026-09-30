package com.betwarrior.pokeapi.model.moves;

import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveMetaData(
		NamedRef<MoveAilment> ailment,
		NamedRef<MoveCategory> category,
		Integer minHits,
		Integer maxHits,
		Integer minTurns,
		Integer maxTurns,
		Integer drain,
		Integer healing,
		Integer critRate,
		Integer ailmentChance,
		Integer flinchChance,
		Integer statChance) {

}
