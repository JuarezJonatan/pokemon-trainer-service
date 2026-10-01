package com.betwarrior.pokestorage.application.exception;

public class SlotAlreadyTakenException extends ConcurrentUpdateException {

	public SlotAlreadyTakenException(Throwable cause) {
		super("The storage slot was taken by a concurrent operation", cause);
	}

}
