# ADR 0001 — Migration from Spring Boot 2.4.3 to 3.5

- **Status:** Accepted
- **Date:** 2026-09-30

## Context

The inherited `pokeapi-reactor` project was built on **Spring Boot 2.4.3 and Java 11**. The challenge asks for a "significant structural evolution" and a new feature (active team / Pokémon box) with persistence, an HTTP API and error handling.

Support status of the versions involved at the time of the decision (source: [endoflife.date/spring-boot](https://endoflife.date/spring-boot)):

| Line | OSS support until | Commercial support until |
|---|---|---|
| 2.4 (inherited) | ended | ended |
| 2.7 (last 2.x) | ended | ended |
| 3.5 (last 3.x) | 2026-06-30 | 2032-06-30 |
| 4.1 (current) | 2027-07-31 | 2028-07-31 |

Building new code on 2.4.3 meant adding features to a base with no security patches, old transitive dependencies (Netty, Reactor, Jackson) and no access to the ecosystem's current tooling.

## Decision

Migrate to **Spring Boot 3.5.16 and Java 17**, in an isolated commit before the new feature, changing in the library **only what the upgrade forces**.

## Reasons

1. **Leave an abandoned base.** 2.x receives no patches; 3.5 has extended commercial support and leaves the project one step (3.5 → 4.x) away from the OSS-supported line.
2. **Java 17.** `record`, `sealed`, pattern matching and switch expressions allow modeling the domain's value objects (IVs, EVs, storage position, origin data) concisely and immutably.
3. **Tooling used by the new feature:**
   - Spring Framework 6's native `ProblemDetail` (RFC 7807) for standard error responses.
   - `@ServiceConnection` + Testcontainers for integration tests against a real Postgres.
   - Current versions of Flyway, R2DBC and springdoc-openapi, which no longer publish releases for Boot 2.
4. **Debt that had to be paid anyway.** The library used `reactor.cache.CacheMono` (reactor-extra), deprecated and removed in reactor-extra 3.5. Any Reactor upgrade broke the cache.

## Changes made to inherited code

All of them forced by the upgrade; no other library behavior was modified.

| Change | Reason |
|---|---|
| `javax.validation` → `jakarta.validation` | Jakarta EE 9+ (Spring Boot 3) |
| `CacheMono` replaced in `ReactiveCacheManagerCacheFacade` with `switchIfEmpty` + `materialize/dematerialize` | Class removed from reactor-extra; the dependency was dropped |
| `PropertyNamingStrategy.SNAKE_CASE` → `PropertyNamingStrategies.SNAKE_CASE` | Constant removed in Jackson 2.13+ |
| `okhttp` / `mockwebserver` with an explicit version (4.12.0) | Spring Boot 3 no longer manages its version |
| JaCoCo 0.8.7 → 0.8.12 | Java 17 bytecode support |
| CI (GitHub Actions) on Java 17 / Temurin | The `adopt` distribution was discontinued |

### The only behavior change, deliberate

`CacheMono` cached any signal, **errors included**: a momentary PokéAPI outage stayed cached until the entry was evicted. The new implementation caches **only values**; errors and empties are propagated without being stored. This is covered by the test `givenACacheMiss_whenTheResourceLookupFails_thenTheErrorIsPropagatedAndNotCached`.

## Alternatives considered

| Alternative | Why it was discarded |
|---|---|
| Stay on 2.4.3 / Java 11 | Zero migration risk, but new code on an unsupported base and without the tooling listed above. |
| 2.7 + Java 17 | Avoids `jakarta` and keeps `CacheMono`, but it is also out of support and has no native `ProblemDetail`. It only postpones the migration. |
| 4.1 (OSS-supported line) | The right mid-term target, but it also means Jackson 3 (`tools.jackson`), which the library relies on heavily to deserialize PokéAPI, and Spring Framework 7. More risk and scope than this challenge justifies. |

## Consequences

- **Positive:** up-to-date base, Java 17 available, deprecated dependency removed, the 21 original tests still pass unmodified (+1 new test).
- **Negative / risks:** the library can no longer be consumed by Spring Boot 2.x projects. Spring Boot 3.5 has already reached the end of its OSS support: **the recommended next step is migrating to 4.x** (future ADR), starting by evaluating the Jackson 2 → 3 migration of the `skaro.pokeapi.resource` classes.
