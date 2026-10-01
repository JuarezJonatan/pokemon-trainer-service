package com.betwarrior.pokeapi.model.pokemon;

import com.betwarrior.pokeapi.model.moves.MoveBattleStyle;
import com.betwarrior.pokeapi.ref.NamedRef;

public record MoveBattleStylePreference(
		Integer lowHpPreference,
		Integer highHpPreference,
		NamedRef<MoveBattleStyle> moveBattleStyle) {

}
