package com.betwarrior.pokestorage.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.betwarrior.pokestorage.domain.StorageCapacity;

@ConfigurationProperties("pokestorage.storage")
public record StorageProperties(int teamCapacity, int boxCapacity) {

	public StorageCapacity capacity() {
		return new StorageCapacity(teamCapacity, boxCapacity);
	}

}
