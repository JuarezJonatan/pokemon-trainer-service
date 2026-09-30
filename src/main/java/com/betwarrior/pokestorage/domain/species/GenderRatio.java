package com.betwarrior.pokestorage.domain.species;

import com.betwarrior.pokestorage.domain.exception.InvalidValueException;
import com.betwarrior.pokestorage.domain.pokemon.Gender;

/**
 * PokeAPI's gender_rate: chance of being female in eighths, or -1 for genderless species.
 */
public record GenderRatio(int femaleEighths) {

	public static final GenderRatio GENDERLESS = new GenderRatio(-1);

	public GenderRatio {
		if (femaleEighths < -1 || femaleEighths > 8) {
			throw new InvalidValueException("Gender rate must be between -1 and 8 but was " + femaleEighths);
		}
	}

	public boolean allows(Gender gender) {
		if (femaleEighths == -1) {
			return gender == Gender.GENDERLESS;
		}
		return switch (gender) {
			case GENDERLESS -> false;
			case MALE -> femaleEighths < 8;
			case FEMALE -> femaleEighths > 0;
		};
	}

}
