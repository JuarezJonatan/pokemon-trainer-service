package com.betwarrior.pokestorage.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.betwarrior.pokestorage.domain.storage.StorageCapacity;

@ConfigurationProperties("pokestorage.storage")
public record StorageProperties(int teamCapacity, int boxCapacity) {

	public StorageCapacity capacity() {
		return new StorageCapacity(teamCapacity, boxCapacity);
	}

}
