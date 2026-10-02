# pokemon-trainer-service

Service that manages **each trainer's individual Pokémon**, separating the **Active Team** from the **PC Box**. Static Pokémon data (species, abilities, moves, items) comes from [PokéAPI](https://pokeapi.co/) through the project's own declarative client, [`com.betwarrior.pokeapi`](#pokéapi-client).

> **TODO:** delete [`docs/SOLUTION.md`](docs/SOLUTION.md) once the technical defense of the challenge is over. It is the detailed write-up of how the challenge was solved (alternatives, findings in the inherited code, pending work) and is kept only for the defense. The summary in [Work done and decisions](#work-done-and-decisions) stays.

## Contents

- [What it does](#what-it-does)
- [How to run it](#how-to-run-it)
- [API](#api)
- [Architecture](#architecture)
- [Domain model and rules](#domain-model-and-rules)
- [Persistence](#persistence)
- [PokéAPI client](#pokéapi-client)
- [Tests](#tests)
- [Maintenance guide](#maintenance-guide)
- [Work done and decisions](#work-done-and-decisions)
- [Tech stack](#tech-stack)

---

## What it does

- **Capture** Pokémon with their individual data: IVs, EVs, nature, ability, gender, shiny, moves, held item and origin data. Each specimen is validated against PokéAPI and **automatically** placed in the first free team slot or, if the team is full, in the box.
- **Detail** of a specimen with all its technical, genetic and origin metadata.
- **Active team** as a **composite view**: the specimen's data is combined with the species data from PokéAPI (types, base stats, sprite) and with the **stats calculated** using the games' official formula.
- Paginated **box**.
- Paginated listings of **all trainers** and **all Pokémon**, across trainers, as utility endpoints.
- **Transfer** between team and box, with capacity validation.
- **Evolution** (bonus): validates that the target species is a **direct** evolution, including branched lines such as Eevee. It reassigns the ability and preserves identity, genetics, held item, history and **the exact slot** in the team.

## How to run it

Requirements: **Java 21** and **Docker** (for Postgres and the integration tests). Maven is not needed: `./mvnw` downloads Maven 3.9 on first use (`mvnw.cmd` on Windows).

```bash
docker compose up -d                 # Postgres 16 on localhost:5432
./mvnw spring-boot:run               # the app on http://localhost:8080
```

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |

Flyway creates the schema on startup. Configuration lives in [`application.yml`](src/main/resources/application.yml) and can be overridden with environment variables:

| Variable / property | Default | Description |
|---|---|---|
| `DB_R2DBC_URL` | `r2dbc:postgresql://localhost:5432/pokestorage` | The app's reactive connection |
| `DB_JDBC_URL` | `jdbc:postgresql://localhost:5432/pokestorage` | JDBC connection, used only for Flyway migrations |
| `DB_USER` / `DB_PASSWORD` | `pokestorage` | Credentials |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | PokéAPI instance |
| `pokestorage.storage.team-capacity` | `6` | Maximum Pokémon in the active team |
| `pokestorage.storage.box-capacity` | `30` | Maximum Pokémon in the box |
| `pokeapi.*` | see [PokéAPI client](#pokéapi-client) | Timeouts, retries and body size towards PokéAPI |
| `spring.cache.caffeine.spec` | `maximumSize=2000,expireAfterWrite=24h` | Cache of PokéAPI resources |

## API

Base path: `/api/v1`. All errors follow **RFC 7807** (`application/problem+json`) and include a stable `code`, so clients don't need to parse messages. This includes the errors Spring raises before a request reaches a controller: they get `malformed-request` (400) or the HTTP status name, e.g. `method-not-allowed`.

The full contract is published as **OpenAPI 3.1** and browsable in **Swagger UI** (`/swagger-ui.html`): every endpoint documents its request fields (with limits and examples), its responses and each error it can return, with an example per `code`. The document is built from annotations on the controllers and payloads, plus [`OpenApiConfiguration`](src/main/java/com/betwarrior/pokestorage/web/openapi/OpenApiConfiguration.java), which holds the API metadata and the reusable error responses.

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/trainers` | Register a trainer | `201` + `Location` |
| `GET` | `/trainers?page=0&size=20` | List all trainers, in registration order (paginated) | `200` |
| `GET` | `/trainers/{trainerId}` | Get a trainer | `200` |
| `GET` | `/pokemon?page=0&size=20` | List every stored Pokémon of all trainers, in storage order (paginated) | `200` |
| `POST` | `/trainers/{trainerId}/pokemon` | Capture / create a specimen (goes to the team or the box) | `201` + `Location` |
| `GET` | `/trainers/{trainerId}/pokemon/{pokemonId}` | Specimen detail | `200` |
| `GET` | `/trainers/{trainerId}/team` | Active team (composite view with PokéAPI) | `200` |
| `GET` | `/trainers/{trainerId}/box?page=0&size=30` | Paginated box | `200` |
| `PUT` | `/trainers/{trainerId}/pokemon/{pokemonId}/storage` | Move between `TEAM` and `BOX` (idempotent) | `200` |
| `POST` | `/trainers/{trainerId}/pokemon/{pokemonId}/evolution` | Evolve a team member | `200` |

### Errors

| HTTP | `code` | When |
|---|---|---|
| 400 | `invalid-value` | An invariant is broken: IV outside 0–31, EV > 252 or total > 510, more than 4 moves, level outside 1–100, page < 0 or size outside 1–100, etc. |
| 400 | `malformed-request` | Malformed JSON, missing or blank required field, invalid enum, UUID or number. `violations` names each culprit, e.g. `nature: invalid value 'BRAVEST' (allowed: [...])` or `species: must not be blank` |
| 404 | `not-found` | Trainer or specimen doesn't exist (or belongs to another trainer) |
| 409 | `storage-full` | On a transfer, the destination is full and `area` says which. On a capture, both team and box are full, so there is no `area` |
| 409 | `pokemon-not-in-team` | Trying to evolve a Pokémon that is in the box |
| 409 | `concurrent-modification` | The Pokémon or the storage kept changing concurrently, even after retrying; safe to retry |
| 422 | `species-rule-violation` | Ability, move or gender incompatible with the species. Includes `violations` with **all** the infractions |
| 422 | `unknown-pokeapi-entry` | Species or item doesn't exist in PokéAPI |
| 422 | `invalid-item` | The given Poké Ball is not a Poké Ball |
| 422 | `evolution-not-allowed` | The target species is not a direct evolution |
| 503 | `pokeapi-unavailable` | PokéAPI doesn't respond (after retries) |

### Examples

```bash
# Register a trainer
curl -s -X POST localhost:8080/api/v1/trainers -H 'Content-Type: application/json' -d '{"name":"Ash"}'

# Capture a Pikachu
curl -s -X POST localhost:8080/api/v1/trainers/$TRAINER/pokemon -H 'Content-Type: application/json' -d '{
  "species": "pikachu",
  "nickname": "Sparky",
  "level": 25,
  "individualValues": {"hp":31,"attack":31,"defense":31,"specialAttack":31,"specialDefense":31,"speed":31},
  "effortValues":     {"hp":4,"attack":0,"defense":0,"specialAttack":252,"specialDefense":0,"speed":252},
  "nature": "TIMID",
  "ability": "static",
  "gender": "FEMALE",
  "shiny": true,
  "moves": ["thunderbolt", "quick-attack"],
  "heldItem": "light-ball",
  "origin": {"pokeball": "poke-ball", "location": "viridian-forest"}
}'
```

Optional fields: `nickname`, `effortValues` (default: all 0), `heldItem`, `origin.originalTrainerId` (default: the capturing trainer), `origin.caughtAt` (default: now). The capture level is recorded as `origin.metLevel`.

```bash
# Active team (composite view)
curl -s localhost:8080/api/v1/trainers/$TRAINER/team
# {"capacity":6,"members":[{"slot":1,
#    "pokemon":{...specimen detail...},
#    "species":{"id":25,"name":"pikachu","types":["electric"],"baseStats":{...},"spriteUrl":"..."},
#    "stats":{"hp":60,"attack":36,"defense":32,"specialAttack":53,"specialDefense":37,"speed":80}}]}

# Every trainer / every Pokémon, page by page
curl -s 'localhost:8080/api/v1/trainers?page=0&size=20'
curl -s 'localhost:8080/api/v1/pokemon?page=0&size=20'
# {"pokemon":[{...specimen detail with trainerId and storage...}],"page":0,"size":20,"totalElements":42,"totalPages":3}

# Deposit into the box / withdraw to the team
curl -s -X PUT localhost:8080/api/v1/trainers/$TRAINER/pokemon/$POKEMON/storage -H 'Content-Type: application/json' -d '{"area":"BOX"}'

# Evolve (ability is optional)
curl -s -X POST localhost:8080/api/v1/trainers/$TRAINER/pokemon/$POKEMON/evolution -H 'Content-Type: application/json' -d '{"targetSpecies":"gyarados"}'
```

Species, ability, move and item names are PokéAPI's (lowercase, hyphenated). Input is normalized: `"Pikachu"` is equivalent to `"pikachu"`.

## Architecture

A modular monolith in a **single Maven module**, organized in layers whose dependencies **only point inwards**. [ArchUnit](src/test/java/com/betwarrior/pokestorage/architecture/ArchitectureTest.java) checks those rules on every build: if someone breaks them, the build fails.

```mermaid
flowchart LR
    client([HTTP client]) --> web

    subgraph service[com.betwarrior.pokestorage]
        web[web<br/>controllers · DTOs · ProblemHandler]
        app[application<br/>use cases · interfaces]
        domain[domain<br/>plain Java]
        subgraph infra[infrastructure]
            persistence[persistence<br/>R2DBC]
            pokeapi[pokeapi<br/>adapter]
        end
    end

    lib[com.betwarrior.pokeapi<br/>PokéAPI client]

    web --> app --> domain
    persistence -. implements .-> app
    pokeapi -. implements .-> app
    pokeapi --> lib
    persistence --> db[(PostgreSQL)]
    lib --> ext[(PokéAPI)]
```

| Package | Responsibility | May depend on |
|---|---|---|
| `domain` | Business rules: value objects, specimen, storage, species, evolution, stat formula | nothing (no Spring, Reactor or the PokéAPI client) |
| `application` | Use cases (`usecase`: `CapturePokemon`, `ListTeam`, `TransferPokemon`, `EvolvePokemon`, …) and the interfaces they need (`port`: `PokemonRepository`, `TrainerRepository`, `PokemonCatalog`) | `domain` |
| `infrastructure.persistence` | R2DBC implementation of the repositories | `application`, `domain` |
| `infrastructure.pokeapi` | `PokeApiPokemonCatalog`: translates PokéAPI records into the domain | `application`, `domain`, PokéAPI client |
| `web` | HTTP: controllers, payloads, format validation, error mapping and OpenAPI documentation | `application`, `domain` |
| `com.betwarrior.pokeapi` | Declarative PokéAPI client and its records | only used by `infrastructure.pokeapi`; it never depends on the service |

Inside each layer, classes are grouped by role or concept, so no layer is a flat list of files:

```text
com.betwarrior.pokestorage
├── domain
│   ├── pokemon        PokemonSpecimen, PokemonId, Level, MoveSet, Gender, CaptureOrigin
│   ├── stats          Stat, StatValues, IndividualValues, EffortValues, Nature, StatCalculator
│   ├── species        Species, SpeciesRef, SpeciesAbility, GenderRatio
│   ├── storage        StorageArea, StorageSlot, StorageCapacity, TrainerStorage
│   ├── trainer        Trainer, TrainerId
│   └── exception      DomainException and its subclasses
├── application
│   ├── usecase
│   │   ├── trainer    RegisterTrainer, FindTrainer, ListTrainers
│   │   ├── pokemon    CapturePokemon, FindPokemon, EvolvePokemon, ListAllPokemon
│   │   └── storage    ListTeam, ListBox, TransferPokemon, ConcurrentUpdateRetry
│   ├── port           PokemonRepository, TrainerRepository, PokemonCatalog
│   ├── pagination     PageRequest (validates page and size), Page<T>
│   ├── exception      ApplicationException and its subclasses
│   └── config         StorageProperties, ApplicationConfiguration
├── infrastructure
│   ├── persistence    R2dbcPokemonRepository, R2dbcTrainerRepository
│   └── pokeapi        PokeApiPokemonCatalog, PokeApiCachingConfiguration
└── web
    ├── controller     TrainerController, PokemonController, AllPokemonController
    ├── dto            Request/response payloads
    ├── error          ProblemHandler (RFC 7807)
    └── openapi        OpenApiConfiguration, ApiProblem
```

Tests mirror the same packages.

### Capture flow

```mermaid
sequenceDiagram
    participant C as Client
    participant W as PokemonController
    participant U as CapturePokemon
    participant D as Domain
    participant P as PokeApiPokemonCatalog
    participant R as R2dbcPokemonRepository

    C->>W: POST /trainers/{id}/pokemon
    W->>U: CapturePokemonCommand
    U->>D: build value objects (IVs, EVs, MoveSet, ...)
    Note over U,D: broken invariant → 400 without calling PokéAPI
    U->>R: does the trainer exist?
    par validation against PokéAPI (cached)
        U->>P: findSpecies(species)
        U->>P: findItem(pokeball)
        U->>P: findItem(held item)
    end
    U->>D: species.validateIndividual(ability, moves, gender)
    U->>R: occupiedSlots(trainer)
    U->>D: TrainerStorage.slotForNewCapture()
    U->>R: insert (transaction)
    Note over U,R: slot taken concurrently → unique violation → retry
    U-->>W: PokemonSpecimen
    W-->>C: 201 Created + Location
```

## Domain model and rules

| Concept | Class | Rule |
|---|---|---|
| Specimen | `PokemonSpecimen` | Immutable; identity given by `PokemonId` (UUID) |
| Individual Values | `IndividualValues` | 6 stats, each between **0 and 31** |
| Effort Values | `EffortValues` | Each stat between **0 and 252**, total **≤ 510** |
| Nature | `Nature` | All **25**, each with the stat it raises and the one it lowers by 10%. The 5 neutral ones change nothing and HP is never affected |
| Ability | `Species.validateIndividual` | Must be one the species has in PokéAPI (hidden ones included) |
| Moves | `MoveSet` | Between **1 and 4**, no duplicates, and learnable by the species according to PokéAPI |
| Gender | `Gender` + `GenderRatio` | `MALE` / `FEMALE` / `GENDERLESS`, compatible with the species' `gender_rate`: genderless → only `GENDERLESS`; 100% male → only `MALE`; etc. |
| Shiny | `shiny` | Flag |
| Origin | `CaptureOrigin` | OT, Poké Ball (must belong to a `*-balls` category in PokéAPI), date, met level and location |
| Held item | `heldItem` | At most one; must exist in PokéAPI |
| Storage | `TrainerStorage` + `StorageSlot` | Capture: first free team slot, otherwise box, otherwise `409`. Slots are **not compacted**: a Pokémon keeps its exact position |
| Calculated stats | `StatCalculator` | Official formula (Gen III+), validated against the reference example of a level 78 Garchomp |
| Evolution | `PokemonSpecimen.evolveInto` | Team only; target = **direct** evolution; everything is preserved except species and ability |

### Evolution

- **Direct-line validation:** a target species is valid if its `evolves_from_species` in PokéAPI is the current species. That covers simple lines (Bulbasaur → Ivysaur → Venusaur, without skipping stages) and branched ones (Eevee → Vaporeon, Jolteon, …), with a single resource per species and without walking the `EvolutionChain` tree.
- **Ability:** if the request specifies one, it must belong to the target species. Otherwise the **same ability slot** is kept (a hidden ability becomes the evolution's hidden ability) and, if that slot doesn't exist, the first non-hidden one is used. Example: Magikarp *swift-swim* (slot 1) → Gyarados *intimidate* (slot 1).
- Situational requirements (level, items, friendship) are not validated, as the statement indicates.

## Persistence

PostgreSQL 16 with **reactive (R2DBC)** access, consistent with WebFlux. The schema is versioned with **Flyway** in [`db/migration`](src/main/resources/db/migration).

- Two tables: `trainer` and `pokemon`. The specimen's attributes are **flat columns** (`iv_hp`, `ev_speed`, `nature`, …), queryable and indexable. Moves are stored in a `varchar[]`.
- **The database also protects the invariants** with `CHECK` constraints (IV/EV ranges, EV total ≤ 510, 1–4 moves, level 1–100), in case someone writes outside the app.
- **Concurrency** is handled with two optimistic mechanisms, and nothing is locked:
  - **Slots:** `UNIQUE (trainer_id, storage_area, storage_slot)`. If two simultaneous captures or transfers pick the same free slot, the database rejects one. Since the domain only assigns slots between 1 and the capacity, **the limit is never exceeded**.
  - **Lost updates:** each Pokémon has a `version` (`V2__add_pokemon_version.sql`). An update only applies `where version = :read` and increments it.
  - **Retry:** both conflicts are retried from a fresh read (`ConcurrentUpdateRetry`), with a jittered backoff and up to as many attempts as there are slots. Every lost race means another operation committed a slot this one hadn't seen, so every capture that fits gets in. An evolution retried after a deposit is rejected with `pokemon-not-in-team`; a deposit retried after an evolution moves the evolved Pokémon.
  - Integration tests fire as many concurrent captures as free slots (all succeed), 20 captures against 8 slots (never more than 8), and an evolution and a deposit of the same Pokémon at once (no accepted change is lost).
- Repositories use `DatabaseClient` with explicit SQL, without Spring Data: the mapping is visible and there is no magic.

## PokéAPI client

`com.betwarrior.pokeapi` is a **Spring HTTP interface**: one method per PokéAPI endpoint, implemented by Spring on top of `WebClient`.

```java
@Autowired PokeApi pokeApi;

pokeApi.pokemon("pikachu");                        // Mono<Pokemon>, by name or id
pokeApi.pokemonSpecies("eevee")
       .flatMap(species -> pokeApi.pokemon(species.defaultVariety().name()));
pokeApi.evolutionChain(1);                          // resources without a name are fetched by id
pokeApi.list("pokemon", 0, 20);                     // Mono<Page<NamedRef<Object>>>, any listing
```

```text
com.betwarrior.pokeapi
├── PokeApi                    @HttpExchange interface, one method per endpoint
├── PokeApiAutoConfiguration   the PokeApi bean, error filter, cache key generator
├── PokeApiProperties          pokeapi.* settings
├── PokeApiJson                snake_case mapping, private to the client
├── Page                       one page of a listing
├── error                      sealed PokeApiException: ResourceNotFound, PokeApiUnavailable, UnexpectedResponse
├── ref                        NamedRef / ApiRef: links between resources, with id()
└── model                      immutable records grouped like the PokéAPI docs
    ├── pokemon · species · evolution · abilities · moves · items · berries
    └── encounters · games · locations · contests · machines · utility
```

- **Any Pokémon, item or move is reachable without code changes.** Only a new *kind* of resource needs code: a record in `model` and one method in `PokeApi`. `PokeApiEndpointsTest` fails if a resource record has no method, and it deserializes a real response for every endpoint.
- **Records with behavior:**
  - Lists are never `null` and cannot be modified.
  - `PokemonSpecies.defaultVariety()` makes species like `deoxys` resolve to `deoxys-normal`; `evolvesFrom()` gives the previous stage.
  - `Pokemon.baseStats()`, `moveNames()`, `learns(move)`, `typeNames()` and `frontSprite()` cover the usual lookups.
  - `Localized.nameIn(language)` returns the name in a given language.
- **Errors:** `PokeApiException` is sealed, so it can be handled exhaustively with a `switch`.
  - 404 → `ResourceNotFoundException`, never retried.
  - 5xx, 429, timeouts and connection errors → retried with backoff, then `PokeApiUnavailableException`.
  - Anything else → `UnexpectedResponseException`.
- **Cache:** `@Cacheable` per endpoint (`pokeapi.pokemon`, `pokeapi.item`, …), active when the application enables caching. This service does it in `PokeApiCachingConfiguration`, backed by Caffeine (24 h, 2000 entries).
  - Names are case-insensitive keys.
  - Failures are never cached.
  - The client switches Caffeine to async mode, which Spring needs to cache `Mono`.

| Property | Default | Description |
|---|---|---|
| `pokeapi.base-url` | `https://pokeapi.co/api/v2` | PokéAPI root (a mirror, or a stub in tests) |
| `pokeapi.connect-timeout` / `response-timeout` | `2s` / `5s` | HTTP timeouts |
| `pokeapi.max-response-size` | `10MB` | Largest body accepted (`/pokemon/mew` is ~670 KB) |
| `pokeapi.retry.max-retries` / `first-backoff` | `2` / `200ms` | Retries on transient failures |

Outside Spring Boot, `PokeApiAutoConfiguration.createClient(WebClient.builder(), properties)` builds the client directly.

**How the service uses it.** `PokeApiPokemonCatalog` makes these calls:
- `pokemonSpecies(name)`, then `pokemon(defaultVariety)`, translated into the domain's `Species`.
- `item(name)` to validate Poké Balls and held items.

`ResourceNotFoundException` becomes "doesn't exist" (422), and any other failure becomes `503`.

## Tests

```bash
./mvnw test      # unit: domain, use cases, PokéAPI client and adapter, ArchUnit (no Docker needed)
./mvnw verify    # + integration with a real Postgres (Testcontainers) and the full app (needs Docker)
```

| Level | What it covers | How |
|---|---|---|
| Domain | Invariants, natures, storage, species rules, evolution, stat formula | JUnit 5 + AssertJ, no mocks |
| Use cases | Orchestration, errors, retry on slot conflicts | In-memory repositories and catalog (`testsupport`) |
| PokéAPI client | Every endpoint maps a real response; errors, retries, caching | `MockWebServer` with trimmed **real** responses, one per endpoint ([`src/test/resources/pokeapi-v2`](src/test/resources/pokeapi-v2)) + `ApplicationContextRunner` |
| PokéAPI adapter | Translation into the domain, 404, unavailability | `MockWebServer` with trimmed **real** PokéAPI responses ([`src/test/resources/pokeapi`](src/test/resources/pokeapi)) |
| Persistence (`*IT`) | Full mapping, slot constraint, pagination | `@DataR2dbcTest` + Testcontainers Postgres |
| API (`*IT`) | All endpoints, error codes, concurrent captures | `@SpringBootTest` + Testcontainers + PokéAPI stub |
| API docs (`*IT`) | The OpenAPI document exposes every endpoint, example and error response | `@SpringBootTest` + `/v3/api-docs` |
| Architecture | Dependencies between layers; the client doesn't know the service | ArchUnit |

Convention: tests follow a **BDD style without Gherkin**. The method name describes the scenario (`givenX_whenY_thenZ`) and phases are separated by blank lines, without comments.

## Maintenance guide

- **Adding a business rule:** it goes in `domain`, in the concept package and object that own the data. For example, a rule about moves goes in `MoveSet` or `Species`. It is tested without Spring.
- **Adding an endpoint:** use case in `application.usecase.<feature>` (one class per use case) → controller method in `web.controller` → payload in `web.dto`. Document it with `@Operation`, `@Schema` on the payload fields and `@ApiResponse(ref = ...)` for its errors. If a new error appears, add the exception to the layer's `exception` package, map it in `ProblemHandler` with a new `code` and add its example to `OpenApiConfiguration`.
- **Changing the schema:** new migration `V{n}__description.sql`. Already-applied migrations are never edited.
- **New PokéAPI data for the service:** add it to `Species` and map it in `PokeApiPokemonCatalog`. For tests, add a trimmed fixture to `src/test/resources/pokeapi/` named `{resource}-{name}.json`.
- **A new kind of PokéAPI resource:** add its record to `com.betwarrior.pokeapi.model.<group>`, a method to `PokeApi` and a real, trimmed response to `src/test/resources/pokeapi-v2/{endpoint}.json`. `PokeApiEndpointsTest` checks both.
- **CI:** [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs `./mvnw verify` on every push and PR.

## Work done and decisions

This project started from [`pokeapi-reactor`](https://github.com/SirSkaro/pokeapi-reactor), an inherited reactive PokéAPI client library, and evolved it into this service.

**Work done**
- **Upgrade:** Spring Boot 2.4 → 3.5 and Java 11 → 21.
- **New PokéAPI client:** `com.betwarrior.pokeapi`, built on Spring HTTP interfaces, with immutable records, typed errors and caching. It replaces the inherited `skaro.pokeapi`, which was deleted, and fixes its mapping bugs.
- **New feature:**
  - individual Pokémon with their genetics, training and origin;
  - the active team and the PC box, with persistence;
  - a REST API with RFC 7807 errors, documented with OpenAPI.
- **Bonus:** evolution, for simple and branched lines, keeping the Pokémon's identity, genetics and exact slot.
- **Hardening after end-to-end testing with curl:**
  - concurrency safety: a unique slot constraint, an optimistic lock and bounded retries;
  - a stable `code` on every error.

**Key decisions**
- **Modular monolith:** a single Maven module organized in layers, with the dependency rules enforced by ArchUnit, instead of a multi-module build.
- **Reactive end to end:** WebFlux + R2DBC, to match the reactive PokéAPI client, instead of MVC + JPA.
- **Database:** PostgreSQL with flat columns and `CHECK` constraints, so the database also enforces the domain invariants, instead of a JSONB column.
- **Concurrency without locks:** a unique constraint on slots plus a version per Pokémon. A lost race is retried from a fresh read.
- **Fixed slots:** slots are never compacted, which keeps a Pokémon's exact slot when it evolves.
- **Stats:**
  - base stats (≥ 1) come from PokéAPI per species;
  - IVs (0–31) are each specimen's own genetics;
  - the final stat uses the games' official formula.

The full detail is in [`docs/SOLUTION.md`](docs/SOLUTION.md): the alternatives considered, the records of the migration and client decisions, the findings in the inherited code, the bugs found, and the pending work.

## Tech stack

- **Java 21**, **Spring Boot 3.5** (WebFlux, Data R2DBC, Validation, Cache, Actuator), **Project Reactor**
- **PostgreSQL 16**, **R2DBC**, **Flyway**
- **Caffeine** (cache), **springdoc-openapi** (Swagger UI)
- **Lombok**, only for constructor injection (`@RequiredArgsConstructor`) and logging (`@Slf4j`)
- **JUnit 5**, **AssertJ**, **Mockito (BDDMockito)**, **Reactor Test**, **Testcontainers**, **OkHttp MockWebServer**, **ArchUnit**, **JaCoCo**
- **Docker Compose**, **GitHub Actions**

MIT license (see [LICENSE](LICENSE)).
