package com.betwarrior.pokestorage.application.exception;

import com.betwarrior.pokestorage.domain.trainer.TrainerId;

public class TrainerNotFoundException extends ApplicationException {

	public TrainerNotFoundException(TrainerId id) {
		super("Trainer %s does not exist".formatted(id));
	}

}
