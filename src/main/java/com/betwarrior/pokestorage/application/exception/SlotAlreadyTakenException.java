package com.betwarrior.pokestorage.application.exception;

public class SlotAlreadyTakenException extends ApplicationException {

	public SlotAlreadyTakenException(Throwable cause) {
		super("The storage slot was taken by a concurrent operation", cause);
	}

}
