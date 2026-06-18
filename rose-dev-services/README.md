# Rose Dev Services

Testcontainers-backed infrastructure for local development and testing ([Arconia-aligned](https://docs.arconia.io/arconia/latest/dev-services/)).

| Module | Artifact | Role |
|--------|----------|------|
| Core | `rose-dev-services-core` | API contracts, `BootstrapMode`, registration, containers |
| Actuator | `rose-dev-services-actuator` | `/actuator/devservices` endpoint (optional) |
| Connectors | `rose-dev-services-{technology}` | Optional runtime dependencies per technology |
| Tests | `rose-dev-services-tests` | Shared integration-test support (test scope) |

Connectors are optional `runtime` dependencies — there is no per-connector starter.

## BootstrapMode (`rose-dev-services-core`)

| Mode | Detection | Behavior |
|------|-----------|----------|
| `TEST` | JUnit / `@SpringBootTest` stack | Containers are not reused |
| `DEV` | DevTools / local `bootRun` | `shared=true` enables reuse |
| `PROD` | Default | Production (connectors must not be on classpath) |

Override: `-Drose.bootstrap.mode=dev|test|prod`

## Enabling

**Tests**: connector on `test` classpath; `rose.dev.services.*.enabled=true` by default.

**Local development**: `runtime` + `optional` (or Gradle `testAndDevelopmentOnly`); `rose.bootstrap.mode=dev`.

**Actuator**: add `rose-dev-services-actuator` when Spring Boot Actuator is on the classpath.

## Module defaults

Connector modules may ship static recommendations under `src/main/resources/rose/default/<name>.properties`.
They are merged into Spring Boot `defaultProperties` by `rose-spring-boot-core`.
Runtime connection values use `DevServicesRegistrar.addDynamicProperty` (highest precedence during dev/test).
