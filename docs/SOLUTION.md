# How the challenge was solved

> **Temporary document.** It exists only to support the technical defense of the *"Pokémon Box and Active Team"* challenge and should be deleted afterwards. Everything needed to use and maintain the project is in the [README](../README.md).

It covers what the [README](../README.md) doesn't: how the statement was interpreted, which decisions were taken and why, what was found in the inherited `pokeapi-reactor` code, which bugs the end-to-end tests surfaced, and what is still pending.

## Contents

- [Starting point](#starting-point)
- [Interpreting the statement](#interpreting-the-statement)
- [Decisions and alternatives](#decisions-and-alternatives)
- [Migration to Spring Boot 3](#migration-to-spring-boot-3)
- [PokéAPI client v2](#pokéapi-client-v2)
- [Changes and findings in the inherited library](#changes-and-findings-in-the-inherited-library)
- [Bugs found by end-to-end testing](#bugs-found-by-end-to-end-testing)
- [Pending work and proposed evolution](#pending-work-and-proposed-evolution)
- [Tools used](#tools-used)

## Starting point

The inherited project, [`pokeapi-reactor`](https://github.com/SirSkaro/pokeapi-reactor), was a non-blocking, caching PokéAPI client, originally published as a library by [SirSkaro](https://github.com/SirSkaro/pokeapi-reactor) (package `skaro.pokeapi`). This project started from it, kept its commit history, and replaced it with [`com.betwarrior.pokeapi`](../README.md#pokéapi-client). The old code was then deleted ([below](#pokéapi-client-v2)); it can still be browsed in git history.

The challenge asked for a new domain feature on top of it (individual Pokémon, active team and PC box, with persistence and an HTTP API) while keeping the project maintainable and easy to onboard into.

## Interpreting the statement

The PDF says stats "start from 1" and that "genetic values" from 0 to 31 are added to them. I modeled it as in the games: the **base stats** (≥ 1) come from PokéAPI per species, and the **IVs** (0–31) are each specimen's own genetics. The final stat combines base, IVs, EVs, level and nature, and is exposed in the team view.

## Decisions and alternatives

**Architecture style.** A layered modular monolith (see the [README](../README.md#architecture)). PokéAPI's model is *snake_case*, huge and designed for the games, not for this business. The domain doesn't know about it: if tomorrow it is replaced by another client, or by a local copy of the data, only `infrastructure.pokeapi` changes. Also, each layer has a clear responsibility, which makes onboarding new people easier.

| Decision | Alternatives considered | Why |
|---|---|---|
| Spring Boot 3.5 + Java 17 ([below](#migration-to-spring-boot-3)), then Java 21 | Stay on 2.4.3; 2.7; 4.1 | 2.x is unsupported. 4.1 means Jackson 3, which the library relies on heavily. 3.5 brings `jakarta` and `ProblemDetail` with limited risk. Java 21 is the current LTS |
| Declarative PokéAPI client on Spring HTTP interfaces ([below](#pokéapi-client-v2)) | Fix `skaro.pokeapi` in place; framework-free client; generate from PokéAPI's OpenAPI spec | Spring already provides the HTTP client, caching and configuration, so the client is an interface plus records. No endpoint registry or `Class` arguments |
| Single module with layers + ArchUnit | Maven multi-module; JPMS | Less ceremony. ArchUnit gives a guarantee similar to separate modules |
| WebFlux + R2DBC | Spring MVC + JPA | The library is reactive; mixing blocking and reactive models adds complexity and the risk of blocking the event loop |
| Flat columns | JSONB with all the genetics | Invariants are also enforced in the database (`CHECK`) and the data is queryable. JSONB is more flexible but opaque |
| Unique constraint for slots + optimistic lock (`version`) per Pokémon | Optimistic lock on the whole trainer; `SELECT … FOR UPDATE` | No long-held locks, and conflicts only happen on an actual race. A version per Pokémon lets operations on different Pokémon of the same trainer run in parallel; the unique constraint protects the capacity |
| Fixed slots (no compaction) | Reorder the team when a Pokémon leaves | The bonus requires preserving the "exact slot" |
| Validate evolution with `evolves_from_species` | Walk the `EvolutionChain` | Simpler: 1 resource per species, already cached by the capture. (The inherited library couldn't even request `EvolutionChain`; the new client can) |
| Team composition in the backend | Let the client call PokéAPI | The statement asks for a composite view; it also centralizes caching and resilience |
| If PokéAPI is down, `GET /team` returns `503` | Degrade: return the team without species data | Simple, explicit contract. Degradation is a possible improvement (see evolution) |
| RFC 7807 errors with `code` | Custom format | Standard, natively supported by Spring 6 |
| IVs required, EVs optional | Everything optional with random defaults | IVs define the individual; a freshly caught Pokémon has no training (EVs at 0) |
| Lombok only for injection constructors and loggers | Lombok everywhere (`@Data`, `@Value`, `@Builder`); no Lombok | Domain objects and payloads are Java `record`s, which already remove the boilerplate Lombok is usually added for. Lombok removes what records can't: constructors that only assign dependencies and logger fields. The domain stays free of it |
| Authorization not implemented | JWT per trainer | Out of scope; left for the proposed evolution |

## Migration to Spring Boot 3

*Decided on 2026-09-30.*

### Context

The inherited `pokeapi-reactor` project was built on **Spring Boot 2.4.3 and Java 11**. The challenge asks for a "significant structural evolution" and a new feature (active team / Pokémon box) with persistence, an HTTP API and error handling.

Support status of the versions involved at the time of the decision (source: [endoflife.date/spring-boot](https://endoflife.date/spring-boot)):

| Line | OSS support until | Commercial support until |
|---|---|---|
| 2.4 (inherited) | ended | ended |
| 2.7 (last 2.x) | ended | ended |
| 3.5 (last 3.x) | 2026-06-30 | 2032-06-30 |
| 4.1 (current) | 2027-07-31 | 2028-07-31 |

Building new code on 2.4.3 meant adding features to a base with no security patches, old transitive dependencies (Netty, Reactor, Jackson) and no access to the ecosystem's current tooling.

### Decision

Migrate to **Spring Boot 3.5.16 and Java 17**, in an isolated commit before the new feature, changing in the library **only what the upgrade forces**.

### Reasons

1. **Leave an abandoned base.** 2.x receives no patches; 3.5 has extended commercial support and leaves the project one step (3.5 → 4.x) away from the OSS-supported line.
2. **Java 17.** `record`, `sealed`, pattern matching and switch expressions allow modeling the domain's value objects (IVs, EVs, storage position, origin data) concisely and immutably.
3. **Tooling used by the new feature:**
   - Spring Framework 6's native `ProblemDetail` (RFC 7807) for standard error responses.
   - `@ServiceConnection` + Testcontainers for integration tests against a real Postgres.
   - Current versions of Flyway, R2DBC and springdoc-openapi, which no longer publish releases for Boot 2.
4. **Debt that had to be paid anyway.** The library used `reactor.cache.CacheMono` (reactor-extra), deprecated and removed in reactor-extra 3.5. Any Reactor upgrade broke the cache.

### Changes made to inherited code

All of them forced by the upgrade; no other library behavior was modified.

| Change | Reason |
|---|---|
| `javax.validation` → `jakarta.validation` | Jakarta EE 9+ (Spring Boot 3) |
| `CacheMono` replaced in `ReactiveCacheManagerCacheFacade` with `switchIfEmpty` + `materialize/dematerialize` | Class removed from reactor-extra; the dependency was dropped |
| `PropertyNamingStrategy.SNAKE_CASE` → `PropertyNamingStrategies.SNAKE_CASE` | Constant removed in Jackson 2.13+ |
| `okhttp` / `mockwebserver` with an explicit version (4.12.0) | Spring Boot 3 no longer manages its version |
| JaCoCo 0.8.7 → 0.8.12 | Java 17 bytecode support |
| CI (GitHub Actions) on Java 17 / Temurin | The `adopt` distribution was discontinued |

#### The only behavior change, deliberate

`CacheMono` cached any signal, **errors included**: a momentary PokéAPI outage stayed cached until the entry was evicted. The new implementation caches **only values**; errors and empties are propagated without being stored. This is covered by the test `givenACacheMiss_whenTheResourceLookupFails_thenTheErrorIsPropagatedAndNotCached`.

### Alternatives considered

| Alternative | Why it was discarded |
|---|---|
| Stay on 2.4.3 / Java 11 | Zero migration risk, but new code on an unsupported base and without the tooling listed above. |
| 2.7 + Java 17 | Avoids `jakarta` and keeps `CacheMono`, but it is also out of support and has no native `ProblemDetail`. It only postpones the migration. |
| 4.1 (OSS-supported line) | The right mid-term target, but it also means Jackson 3 (`tools.jackson`), which the library relies on heavily to deserialize PokéAPI, and Spring Framework 7. More risk and scope than this challenge justifies. |

### Consequences

- **Positive:** up-to-date base, Java 17 available, deprecated dependency removed, the 21 original tests still pass unmodified (+1 new test).
- **Negative / risks:** the library can no longer be consumed by Spring Boot 2.x projects. Spring Boot 3.5 has already reached the end of its OSS support: **the recommended next step is migrating to 4.x**, starting with the Jackson 2 → 3 migration of the PokéAPI model (now `com.betwarrior.pokeapi`, see [Pending work](#pending-work-and-proposed-evolution)).

## PokéAPI client v2

*Decided on 2026-09-30.*

### Context

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

### Decision

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

### Alternatives considered

| Alternative | Why not |
|---|---|
| Fix `skaro.pokeapi` in place | Its public API is the problem: fixing typing, errors and caching changes every signature anyway |
| Framework-free core (reactor-netty + Jackson) with an optional Spring adapter | Portable, but it re-implements what Spring already provides (HTTP client, caching, configuration) for a project that is Spring-only. An initial attempt was discarded for being more complex than the problem |
| Generate the client from PokéAPI's OpenAPI spec | Full coverage, but the generated model is anemic again and hard to enrich |
| Separate Maven module or repository | Cleaner boundary, but more build and release overhead. ArchUnit already enforces that the client does not depend on the service |

### Consequences

- **Adding data is free:** a new Pokémon, item or move needs no code change.
- **Adding a new kind of resource is small:** it takes a record and one interface method. `PokeApiEndpointsTest` fails if a resource record has no method, and it deserializes a real response for every endpoint.
- **The service adapter shrinks:** `PokeApiPokemonCatalog` only translates records into the domain.
- **Spring dependency:** the client depends on Spring (`spring-webflux`, `spring-context`), which is acceptable for this codebase.
- **Cache keys:** entries are keyed by what the caller asked for, so `"25"` and `"pikachu"` are cached separately.
- **Unmapped fields:** a few PokéAPI fields with deep or undocumented shapes are not mapped (`sprites.other`, `sprites.versions`, `Type.sprites`, newer `EvolutionDetail` conditions). They are ignored, never an error.

### Removal of `skaro.pokeapi`

The first plan was to keep `skaro.pokeapi` for one release, deprecated for removal, so that its users could migrate. Nothing depends on it anymore: the service was migrated in the same change, and the library was never published from this repository. Keeping it would only preserve 104 mutable classes with setters, so it was **deleted in the same change**, together with its tests. Its history remains in git.

## Changes and findings in the inherited library

While solving the challenge, the criterion was: **only what the upgrade required or what blocked the feature was modified.** Everything else was documented. The library has since been **replaced and deleted**; every finding below is fixed in the new client ([below](#pokéapi-client-v2)). The tables are kept as a record of why.

### Modified

| Change | Reason |
|---|---|
| `javax.validation` → `jakarta.validation` | Upgrade to Boot 3 |
| `CacheMono` replaced in `ReactiveCacheManagerCacheFacade` | Class removed in reactor-extra 3.5. As a side effect, **errors are no longer cached**: before, a momentary PokéAPI outage stayed in the cache |
| `PropertyNamingStrategy.SNAKE_CASE` → `PropertyNamingStrategies.SNAKE_CASE` | Removed in Jackson 2.13+ |
| **`Item.category` was a list, but PokéAPI returns an object** | **No `Item` could be deserialized**, and the feature needs to validate the Poké Ball and the held item. Regression test with a real response |

### Detected, not modified

| # | Finding | Impact | Possible fix |
|---|---|---|---|
| 1 | `ReactiveCachingPokeApiClient` caches every listing (`getResource(Class)` and paginated ones) in the `NamedApiResourceList` cache under the key `"collection"`, **regardless of type** | Listing abilities after listing Pokémon returns the Pokémon list | Include the resource class in the key or in the cache name |
| 2 | The `Region` endpoint is registered as `pokemon-region`; the correct one is `region` | `getResource(Region.class, …)` always fails (PokéAPI responds 400) | Fix the mapping in `PokeApiReactorEndpointConfiguration` |
| 3 | `EvolutionChain` doesn't implement `PokeApiResource` | It can't be requested with the client: the generics prevent it | Implement the interface (`getName()` → `null`) |
| 4 | The original README documents the property `skaro.pokeapi.max-buffer-size`, but the real one is `max-bytes-to-buffer` | The documented setting is silently ignored | Fix the documentation or add an alias |
| 5 | The default buffer (565 KB) is smaller than the `/pokemon/mew` response (~670 KB) | Deserialization error on Pokémon with many moves | Raise the default. This service sets it to 10 MB |
| 6 | Fields that don't match PokéAPI's JSON: `BerryFlavor.barries`, `ContestComboDetail.userBefore`/`userAfter`, `LocationArea.Id`/`encoutnerMethodRates`, `EvolutionChain.item` (really `baby_trigger_item`), `Item.cost` (now `prices`) | That data is silently `null` | Found by comparing every record with a real response while writing the new client |

In addition, Spring Boot 2.4, reactor-extra and the `adopt` Java distribution were discontinued. The upgrade solved all of that.

## Bugs found by end-to-end testing

Besides the automated tests, the running service was exercised with a curl suite: 256 cases against the real PokéAPI, covering every endpoint, every error code and concurrency. It found three problems that the unit and integration tests had missed, all fixed with a test that reproduces each one:

| Problem | Cause | Fix |
|---|---|---|
| Captures rejected with free slots left: with 6 simultaneous captures and room for 9, only 4–5 got in | Only 3 immediate retries after a slot conflict | Retries bounded by the number of slots, with a jittered backoff (`ConcurrentUpdateRetry`). Every lost race means another capture took a slot, so every capture that fits gets in |
| An evolution and a deposit of the same Pokémon at once both answered 200, but the evolution was silently lost | Read-modify-write without any version check: the last write won | Optimistic lock: a `version` per Pokémon (`V2__add_pokemon_version.sql`); a stale update is retried from a fresh read |
| Errors raised by Spring (malformed JSON, invalid enum, UUID or number, blank fields) had no `code` | They never reached `ProblemHandler`'s mappings | `malformed-request` with the offending fields in `violations`; other framework errors use the HTTP status name |

One case was left as is on purpose: a capture into full storage answers `storage-full` without `area`, because both team and box are full. It is documented, not a bug.

## Pending work and proposed evolution

1. **Spring Boot 4.x**: 3.5 no longer has OSS support. It requires migrating the PokéAPI client (`com.betwarrior.pokeapi`) to Jackson 3 (see [Migration to Spring Boot 3](#migration-to-spring-boot-3)).
2. **Authentication and authorization** per trainer (OAuth2/JWT): today anyone with the ID can operate on any trainer.
3. **Extract `com.betwarrior.pokeapi`** into its own module or repository with semantic versioning if another service needs it.
4. **Graceful degradation** of `GET /team` when PokéAPI doesn't respond: return the specimen data with `species: null` and a warning.
5. **Distributed cache** (Redis) if the service scales horizontally, or a **local PokéAPI replica** (it's open source) to avoid depending on a rate-limited public service.
6. **Observability:** Micrometer metrics (PokéAPI latency, cache hit ratio, slot conflicts) and tracing.
7. **Domain events** (`PokemonCaptured`, `PokemonEvolved`) with an outbox, if other services need to know.
8. **Multiple PC boxes** (Box 1..N), as in the games: `StorageSlot` already models area + position and would be extended with a box number.
9. Missing operations: release a Pokémon, change the held item, reorder the team (slot swap), level up or train EVs.

## Tools used

- **Java 21**, **Spring Boot 3.5** (WebFlux, Data R2DBC, Validation, Cache, Actuator), **Project Reactor**
- **PostgreSQL 16**, **R2DBC**, **Flyway**
- **Caffeine** (cache), **springdoc-openapi** (Swagger UI)
- **Lombok**, only for constructor injection (`@RequiredArgsConstructor`) and logging (`@Slf4j`)
- **JUnit 5**, **AssertJ**, **Mockito (BDDMockito)**, **Reactor Test**, **Testcontainers**, **OkHttp MockWebServer**, **ArchUnit**, **JaCoCo**
- **Docker Compose**, **GitHub Actions**
- **Claude Code** as an AI assistant during design, implementation and review

---

