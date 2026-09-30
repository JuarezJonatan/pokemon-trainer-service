package com.betwarrior.pokestorage.domain.exception;

import com.betwarrior.pokestorage.domain.storage.StorageArea;

public class StorageFullException extends DomainException {

	private final StorageArea area;

	private StorageFullException(StorageArea area, String message) {
		super(message);
		this.area = area;
	}

	public static StorageFullException teamAndBox() {
		return new StorageFullException(null, "Both the active team and the PC box are full");
	}

	public static StorageFullException of(StorageArea area, int capacity) {
		return new StorageFullException(area, "The %s is full (capacity %d)".formatted(describe(area), capacity));
	}

	public StorageArea area() {
		return area;
	}

	private static String describe(StorageArea area) {
		return area == StorageArea.TEAM ? "active team" : "PC box";
	}

}
