package com.betwarrior.pokestorage.application;

import com.betwarrior.pokestorage.domain.TrainerId;

public class TrainerNotFoundException extends ApplicationException {

	public TrainerNotFoundException(TrainerId id) {
		super("Trainer %s does not exist".formatted(id));
	}

}
