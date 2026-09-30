package com.betwarrior.pokestorage.domain.stats;

import static com.betwarrior.pokestorage.domain.stats.Stat.ATTACK;
import static com.betwarrior.pokestorage.domain.stats.Stat.DEFENSE;
import static com.betwarrior.pokestorage.domain.stats.Stat.SPECIAL_ATTACK;
import static com.betwarrior.pokestorage.domain.stats.Stat.SPECIAL_DEFENSE;
import static com.betwarrior.pokestorage.domain.stats.Stat.SPEED;

/**
 * The 25 natures. Each one raises a stat by 10% and lowers another by 10%;
 * the five neutral natures raise and lower the same stat, cancelling out.
 * HP is never affected.
 */
public enum Nature {
	HARDY(ATTACK, ATTACK), LONELY(ATTACK, DEFENSE), BRAVE(ATTACK, SPEED), ADAMANT(ATTACK, SPECIAL_ATTACK), NAUGHTY(ATTACK, SPECIAL_DEFENSE),
	BOLD(DEFENSE, ATTACK), DOCILE(DEFENSE, DEFENSE), RELAXED(DEFENSE, SPEED), IMPISH(DEFENSE, SPECIAL_ATTACK), LAX(DEFENSE, SPECIAL_DEFENSE),
	TIMID(SPEED, ATTACK), HASTY(SPEED, DEFENSE), SERIOUS(SPEED, SPEED), JOLLY(SPEED, SPECIAL_ATTACK), NAIVE(SPEED, SPECIAL_DEFENSE),
	MODEST(SPECIAL_ATTACK, ATTACK), MILD(SPECIAL_ATTACK, DEFENSE), QUIET(SPECIAL_ATTACK, SPEED), BASHFUL(SPECIAL_ATTACK, SPECIAL_ATTACK), RASH(SPECIAL_ATTACK, SPECIAL_DEFENSE),
	CALM(SPECIAL_DEFENSE, ATTACK), GENTLE(SPECIAL_DEFENSE, DEFENSE), SASSY(SPECIAL_DEFENSE, SPEED), CAREFUL(SPECIAL_DEFENSE, SPECIAL_ATTACK), QUIRKY(SPECIAL_DEFENSE, SPECIAL_DEFENSE);

	private final Stat increased;
	private final Stat decreased;

	Nature(Stat increased, Stat decreased) {
		this.increased = increased;
		this.decreased = decreased;
	}

	public Stat increased() {
		return increased;
	}

	public Stat decreased() {
		return decreased;
	}

	public boolean isNeutral() {
		return increased == decreased;
	}

	public double modifierFor(Stat stat) {
		if (isNeutral()) {
			return 1.0;
		}
		if (stat == increased) {
			return 1.1;
		}
		if (stat == decreased) {
			return 0.9;
		}
		return 1.0;
	}

}
