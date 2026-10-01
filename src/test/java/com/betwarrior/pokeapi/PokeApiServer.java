package com.betwarrior.pokeapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * PokéAPI double: {@code /<endpoint>/<anything>} answers with {@code pokeapi-v2/<endpoint>.json} and
 * {@code /<endpoint>?...} with {@code pokeapi-v2/<endpoint>-listing.json}, unless a status was queued for it.
 */
public class PokeApiServer implements AutoCloseable {

	private final MockWebServer server = new MockWebServer();
	private final Map<String, Queue<Integer>> queuedStatuses = new ConcurrentHashMap<>();
	private final List<String> requests = new CopyOnWriteArrayList<>();

	public PokeApiServer() {
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
		return server.url("/api/v2").toString();
	}

	public void respondWith(String path, Integer... statuses) {
		queuedStatuses.computeIfAbsent(path, key -> new ArrayDeque<>()).addAll(List.of(statuses));
	}

	public List<String> requests() {
		return List.copyOf(requests);
	}

	public long requestsTo(String path) {
		return requests.stream().filter(path::equals).count();
	}

	@Override
	public void close() throws IOException {
		server.shutdown();
	}

	private MockResponse respond(String rawPath) {
		String path = rawPath.replaceFirst("^/api/v2/", "");
		requests.add(path);
		Integer status = queuedStatuses.getOrDefault(path, new ArrayDeque<>()).poll();
		if (status != null) {
			return new MockResponse().setResponseCode(status);
		}
		String fixture = path.contains("?")
				? path.substring(0, path.indexOf('?')) + "-listing"
				: path.substring(0, path.indexOf('/'));
		try (InputStream body = PokeApiServer.class.getResourceAsStream("/pokeapi-v2/" + fixture + ".json")) {
			if (body == null) {
				return new MockResponse().setResponseCode(404);
			}
			return new MockResponse()
					.setHeader("Content-Type", "application/json")
					.setBody(new String(body.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
