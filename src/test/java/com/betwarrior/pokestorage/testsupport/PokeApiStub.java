package com.betwarrior.pokestorage.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * PokeAPI double serving trimmed real responses from {@code src/test/resources/pokeapi}.
 * Links inside the responses are rewritten to point back to the stub.
 */
public class PokeApiStub implements AutoCloseable {

	private static final String REAL_BASE_URL = "https://pokeapi.co/api/v2/";

	private final MockWebServer server = new MockWebServer();
	private final Map<String, Integer> forcedStatuses = new ConcurrentHashMap<>();
	private final Map<String, AtomicInteger> requests = new ConcurrentHashMap<>();

	public PokeApiStub() {
		server.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				return respond(request.getPath());
			}
		});
		try {
			server.start();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String baseUrl() {
		return server.url("/api/v2/").toString();
	}

	public void failWith(String path, int status) {
		forcedStatuses.put(path, status);
	}

	public int requestsTo(String path) {
		return requests.getOrDefault(path, new AtomicInteger()).get();
	}

	@Override
	public void close() throws IOException {
		server.shutdown();
	}

	private MockResponse respond(String path) {
		String resource = path.replaceFirst("^/api/v2/", "").replaceAll("/$", "");
		requests.computeIfAbsent(resource, key -> new AtomicInteger()).incrementAndGet();
		Integer forced = forcedStatuses.get(resource);
		if (forced != null) {
			return new MockResponse().setResponseCode(forced);
		}
		String fixture = "/pokeapi/" + resource.replace('/', '-') + ".json";
		try (InputStream body = PokeApiStub.class.getResourceAsStream(fixture)) {
			if (body == null) {
				return new MockResponse().setResponseCode(404).setBody("Not Found");
			}
			String json = new String(body.readAllBytes(), StandardCharsets.UTF_8).replace(REAL_BASE_URL, baseUrl());
			return new MockResponse().setHeader("Content-Type", "application/json").setBody(json);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
