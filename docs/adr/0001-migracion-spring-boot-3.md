# ADR 0001 — Migración de Spring Boot 2.4.3 a 3.5

- **Estado:** Aceptada
- **Fecha:** 2026-09-30

## Contexto

El proyecto heredado `pokeapi-reactor` estaba construido sobre **Spring Boot 2.4.3 y Java 11**. El desafío pide una "evolución estructural importante" y la incorporación de una funcionalidad nueva (equipo activo / baúl de Pokémon) con persistencia, API HTTP y manejo de errores.

Situación de soporte de las versiones involucradas al momento de la decisión (fuente: [endoflife.date/spring-boot](https://endoflife.date/spring-boot)):

| Línea | Soporte OSS hasta | Soporte comercial hasta |
|---|---|---|
| 2.4 (heredada) | vencido | vencido |
| 2.7 (última 2.x) | vencido | vencido |
| 3.5 (última 3.x) | 2026-06-30 | 2032-06-30 |
| 4.1 (actual) | 2027-07-31 | 2028-07-31 |

Construir código nuevo sobre 2.4.3 implicaba sumar funcionalidad a una base sin parches de seguridad, con dependencias transitivas antiguas (Netty, Reactor, Jackson) y sin acceso a herramientas actuales del ecosistema.

## Decisión

Migrar a **Spring Boot 3.5.16 y Java 17**, en un commit aislado y previo a la funcionalidad nueva, modificando en la librería **solamente lo que el upgrade obliga a cambiar**.

## Motivos

1. **Salir de una base abandonada.** 2.x no recibe parches; 3.5 mantiene soporte comercial extendido y deja el proyecto a un paso (3.5 → 4.x) de la línea con soporte OSS.
2. **Java 17.** `record`, `sealed`, pattern matching y switch expressions permiten modelar value objects del dominio (IVs, EVs, posición de almacenamiento, datos de origen) de forma concisa e inmutable.
3. **Herramientas que usa la funcionalidad nueva:**
   - `ProblemDetail` (RFC 7807) nativo de Spring Framework 6 para respuestas de error estándar.
   - `@ServiceConnection` + Testcontainers para tests de integración contra Postgres real.
   - Versiones actuales de Flyway, R2DBC y springdoc-openapi, que ya no publican versiones para Boot 2.
4. **Deuda que había que pagar igual.** La librería usaba `reactor.cache.CacheMono` (reactor-extra), deprecado y eliminado en reactor-extra 3.5. Cualquier actualización de Reactor rompía la caché.

## Cambios realizados en código heredado

Todos forzados por el upgrade; ningún otro comportamiento de la librería fue modificado.

| Cambio | Motivo |
|---|---|
| `javax.validation` → `jakarta.validation` | Jakarta EE 9+ (Spring Boot 3) |
| `CacheMono` reemplazado en `ReactiveCacheManagerCacheFacade` por `switchIfEmpty` + `materialize/dematerialize` | Clase eliminada de reactor-extra; se quitó la dependencia |
| `PropertyNamingStrategy.SNAKE_CASE` → `PropertyNamingStrategies.SNAKE_CASE` | Constante eliminada en Jackson 2.13+ |
| `okhttp` / `mockwebserver` con versión explícita (4.12.0) | Spring Boot 3 dejó de gestionar su versión |
| JaCoCo 0.8.7 → 0.8.12 | Soporte de bytecode Java 17 |
| CI (GitHub Actions) a Java 17 / Temurin | Distribución `adopt` discontinuada |

### Única diferencia de comportamiento, deliberada

`CacheMono` cacheaba cualquier señal, **incluidos errores**: una caída momentánea de PokéAPI quedaba cacheada hasta la expulsión de la entrada. La nueva implementación cachea **solo valores**; errores y vacíos se propagan sin persistirse. Está cubierto por el test `givenACacheMiss_whenTheResourceLookupFails_thenTheErrorIsPropagatedAndNotCached`.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Quedarse en 2.4.3 / Java 11 | Cero riesgo de migración, pero código nuevo sobre una base sin soporte y sin las herramientas listadas arriba. |
| 2.7 + Java 17 | Evita `jakarta` y conserva `CacheMono`, pero también está fuera de soporte y no tiene `ProblemDetail` nativo. Sólo posterga la migración. |
| 4.1 (línea con soporte OSS) | Es el destino correcto a mediano plazo, pero implica además Jackson 3 (`tools.jackson`), del que la librería depende fuertemente para deserializar PokéAPI, y Spring Framework 7. Mayor riesgo y alcance del que justifica este desafío. |

## Consecuencias

- **Positivas:** base actualizada, Java 17 disponible, dependencia deprecada eliminada, los 21 tests originales siguen en verde sin modificarse (+1 test nuevo).
- **Negativas / riesgos:** la librería deja de ser consumible por proyectos Spring Boot 2.x. Spring Boot 3.5 ya terminó su soporte OSS: **el siguiente paso recomendado es migrar a 4.x** (ADR futuro), empezando por evaluar la migración de Jackson 2 → 3 en los recursos de `skaro.pokeapi.resource`.
