# ADR 0002 — A declarative PokéAPI client to replace `skaro.pokeapi`

- **Status:** Accepted
- **Date:** 2026-09-30

## Context

The inherited `skaro.pokeapi` library works, but it is hard to use and hard to evolve.

| Problem | Consequence |
|---|---|
| 103 mutable POJOs with setters and nullable `Integer`/`Boolean` | No behavior on the model; every consumer rewrites the same lookups and null checks (`Boolean.TRUE.equals(variety.getIsDefault())`) |
| Resources resolved as `Class` → `Map<Class, String>` at runtime | An unregistered class or a wrong path (`Region` → `pokemon-region`) compiles and fails at runtime; `EvolutionChain` cannot be requested at all |
| Three overloaded `getResource` plus `followResource(Supplier, Class)` | An API that is hard to discover and read |
| Hand-written cache on top of `CacheManager`, duplicated in two client classes | All listings share one cache key (a Pokémon listing can come back as an ability listing); caches silently disabled unless created lazily |
| No error model | `WebClientResponseException` leaks to consumers; no timeouts or retries |
| Only usable through `@Import` of `@Configuration` classes | Registers generic `ObjectMapper` codecs and a `WebClient` in the consumer's context |

Checking the POJOs against real PokéAPI responses also revealed mapping bugs that silently dropped data:
- Misspelled fields: `BerryFlavor.barries`, `ContestComboDetail.userBefore`/`userAfter` and `LocationArea.encoutnerMethodRates`.
- `LocationArea.Id`.
- `EvolutionChain.item` should be `baby_trigger_item`.
- `Item.cost` no longer exists; PokéAPI now returns `prices`.

## Decision

Replace it with **`com.betwarrior.pokeapi`**, built on the Spring features the project already uses. It lives in the same Maven module, in its own package.

- **Client:** `PokeApi` is a **Spring HTTP interface** (`@HttpExchange`), with one method per endpoint:
  - Named endpoints take `pokemon(String nameOrId)`; endpoints without names take `evolutionChain(int id)`.
  - `list(endpoint, offset, limit)` pages any listing.
  - Spring generates the implementation on top of `WebClient`, so there is no endpoint registry, no `Class` argument and no hand-written HTTP code.
- **Configuration:** `PokeApiAutoConfiguration` exposes the bean, configured under `pokeapi.*`:
  - base URL, timeouts, maximum body size and retries;
  - every setting has a default.
  - The JSON mapping (`PokeApiJson`) is private to the client.
- **Errors:** an `ExchangeFilterFunction` maps responses into a sealed `PokeApiException` hierarchy:
  - 404 → `ResourceNotFoundException`.
  - 5xx, 429 and connection errors → retried with backoff, then `PokeApiUnavailableException`.
  - Anything else → `UnexpectedResponseException`.
- **Cache:** `@Cacheable` on each method, one cache per endpoint.
  - The key generator lowercases names, because PokéAPI treats them case-insensitively.
  - Caching is active when the application enables it. Caffeine is switched to async mode, which Spring requires to cache `Mono`.
- **Model:** immutable **records** grouped like the PokéAPI docs (`pokemon`, `species`, `items`, `moves`, ...):
  - Lists are never `null` and cannot be modified.
  - The mapping bugs above are fixed, and every endpoint PokéAPI serves today is covered.
  - The resources consumers use most carry behavior: `PokemonSpecies.defaultVariety()`, `evolvesFrom()`, `Pokemon.baseStats()`, `learns()`, `typeNames()`, and `Localized.nameIn(language)`.

## Alternatives considered

| Alternative | Why not |
|---|---|
| Fix `skaro.pokeapi` in place | Its public API is the problem: fixing typing, errors and caching changes every signature anyway |
| Framework-free core (reactor-netty + Jackson) with an optional Spring adapter | Portable, but it re-implements what Spring already provides (HTTP client, caching, configuration) for a project that is Spring-only. An initial attempt was discarded for being more complex than the problem |
| Generate the client from PokéAPI's OpenAPI spec | Full coverage, but the generated model is anemic again and hard to enrich |
| Separate Maven module or repository | Cleaner boundary, but more build and release overhead. ArchUnit already enforces that the client does not depend on the service |

## Consequences

- **Adding data is free:** a new Pokémon, item or move needs no code change.
- **Adding a new kind of resource is small:** it takes a record and one interface method. `PokeApiEndpointsTest` fails if a resource record has no method, and it deserializes a real response for every endpoint.
- **The service adapter shrinks:** `PokeApiPokemonCatalog` only translates records into the domain.
- **Spring dependency:** the client depends on Spring (`spring-webflux`, `spring-context`), which is acceptable for this codebase.
- **Cache keys:** entries are keyed by what the caller asked for, so `"25"` and `"pikachu"` are cached separately.
- **Unmapped fields:** a few PokéAPI fields with deep or undocumented shapes are not mapped (`sprites.other`, `sprites.versions`, `Type.sprites`, newer `EvolutionDetail` conditions). They are ignored, never an error.

## Coexistence and removal

`skaro.pokeapi` stays for one release, with its entry points marked `@Deprecated(since = "2.0", forRemoval = true)` and its original tests still running.
- ArchUnit forbids both the service and the new client from depending on it.
- It will be deleted in the next release, together with its tests and fixtures.
