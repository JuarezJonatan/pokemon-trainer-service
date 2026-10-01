package com.betwarrior.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.service.annotation.GetExchange;

import com.betwarrior.pokeapi.model.NamedResource;
import com.betwarrior.pokeapi.model.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import reactor.core.publisher.Mono;

class PokeApiEndpointsTest {

	private final PokeApiServer server = new PokeApiServer();
	private final ApplicationContextRunner context = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PokeApiAutoConfiguration.class))
			.withPropertyValues("pokeapi.base-url=" + server.baseUrl());

	@AfterEach
	void stopServer() throws IOException {
		server.close();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("resourceMethods")
	void givenARealPokeApiResponse_whenFetchingTheEndpoint_thenItIsMappedToItsRecord(Method method) {
		context.run(started -> {
			PokeApi pokeApi = started.getBean(PokeApi.class);
			Object argument = method.getParameterTypes()[0] == int.class ? 1 : "1";

			Resource resource = fetch(pokeApi, method, argument);

			assertThat(resource).isInstanceOf(resourceType(method));
			assertThat(resource.id()).isEqualTo(fixtureId(endpointOf(method)));
			if (resource instanceof NamedResource named) {
				assertThat(named.name()).isNotBlank();
			}
		});
	}

	@Test
	void givenTheResourceModel_whenLookingForItsEndpoints_thenEveryResourceCanBeFetched() {
		Set<Class<?>> fetchable = resourceMethods().map(PokeApiEndpointsTest::resourceType).collect(Collectors.toSet());

		Set<Class<?>> resources = new ClassFileImporter().importPackages("com.betwarrior.pokeapi.model").stream()
				.filter(type -> type.isAssignableTo(Resource.class) && !type.isInterface())
				.map(JavaClass::reflect)
				.collect(Collectors.toSet());

		assertThat(fetchable).isEqualTo(resources);
	}

	static Stream<Method> resourceMethods() {
		return Arrays.stream(PokeApi.class.getDeclaredMethods())
				.filter(method -> method.getParameterCount() == 1)
				.sorted((left, right) -> left.getName().compareTo(right.getName()));
	}

	private static Resource fetch(PokeApi pokeApi, Method method, Object argument) {
		try {
			return (Resource) ((Mono<?>) method.invoke(pokeApi, argument)).block();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Class<?> resourceType(Method method) {
		return (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
	}

	private static String endpointOf(Method method) {
		String path = method.getAnnotation(GetExchange.class).value();
		return path.substring(1, path.indexOf("/{"));
	}

	private static int fixtureId(String endpoint) {
		try (InputStream json = PokeApiEndpointsTest.class.getResourceAsStream("/pokeapi-v2/" + endpoint + ".json")) {
			return new ObjectMapper().readTree(json).get("id").asInt();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
