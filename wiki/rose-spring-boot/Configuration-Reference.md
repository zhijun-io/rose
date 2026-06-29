# Configuration Reference

Key properties, module layout, and customization points for the Rose repository.

---

## Version (`${revision}`)

| Item              | Location                                           |
|-------------------|----------------------------------------------------|
| Source of truth   | `rose-build/pom.xml` → `<revision>`                |
| Current value     | `0.0.1-SNAPSHOT`                                   |
| Override at build | `mvn test -Drevision=1.0.0-SNAPSHOT`               |
| Flattening        | `flatten-maven-plugin` (`resolveCiFriendliesOnly`) |

Consumer applications pin **`rose-bom`** version in `dependencyManagement`; they do not use `${revision}`.

---

## Three-Layer Maven Layout

| Layer  | Artifact      | Path           | Consumer inherits? |
|--------|---------------|----------------|--------------------|
| Build  | `rose-build`  | `rose-build/`  | No                 |
| Parent | `rose-parent` | root `pom.xml` | No (reactor only)  |
| BOM    | `rose-bom`    | `rose-bom/`    | Yes (import)       |

---

## Reactor Modules

| Directory           | Description                             |
|---------------------|-----------------------------------------|
| `rose-build`        | Shared build parent POM                 |
| `rose-bom`          | Bill of Materials for consumers         |
| `rose-core`         | Core utilities                          |
| `rose-spring`       | Spring Framework extensions             |
| `rose-spring-boot`  | Spring Boot utilities and starters      |
| `rose-mybatis-plus` | MyBatis-Plus extensions                 |
| `rose-observation`  | Observation / OpenTelemetry integration |
| `rose-multitenancy` | Multitenancy core, spring, starter      |
| `rose-devservice`   | Testcontainers dev services             |

---

## Locked Dependency Versions (reactor)

Defined in root `rose-parent` `properties`:

| Property                 | Purpose                                       |
|--------------------------|-----------------------------------------------|
| `spring-boot.version`    | Spring Boot BOM (2.7.18; includes Micrometer) |
| `testcontainers.version` | Testcontainers BOM                            |
| `opentelemetry.version`  | OpenTelemetry SDK BOM                         |
| `mybatis-plus.version`   | MyBatis-Plus                                  |

Third-party versions are **not** re-exported through `rose-bom`; consumers rely on `spring-boot-starter-parent` for non-
`io.zhijun` coordinates.

---

## Resource Filtering

Inherited from `rose-build`:

```xml
<resources>
    <resource>
        <directory>src/main/java</directory>
        <filtering>true</filtering>
        <includes>
            <include>**/*.properties</include>
            <include>**/*.xml</include>
        </includes>
    </resource>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
    </resource>
</resources>
```

Delimiter: `@` (`resource.delimiter` property).

---

## Enforcer Rules (`validate` phase)

| Rule                                | Constraint                                |
|-------------------------------------|-------------------------------------------|
| `requireMavenVersion`               | Maven ≥ 3.6                               |
| `requireJavaVersion`                | Build JDK ≥ 8                             |
| `banDuplicatePomDependencyVersions` | No duplicate dependency versions in a POM |
| `bannedDependencies`                | Placeholder for future restrictions       |

---

## Application Configuration (Rose runtime)

See [Consumer Guide](../rose-bom/Consumer-Guide) for starter-specific prefixes:

| Prefix                                 | Scope                                                          |
|----------------------------------------|----------------------------------------------------------------|
| `rose.observation.*`                   | Observation domain toggle and conventions backend selection    |
| `rose.otel.*`                          | OpenTelemetry SDK (traces, metrics, logs, exporters, resource) |
| `rose.otel.exporter.otlp.micrometer.*` | Micrometer OTLP registry (when bridge disabled)                |
| `rose.otel.exporter.otlp.tls.*`        | OTLP TLS (`trusted-certificates`, `certificate`, `key`)        |
| `rose.otel.exporter.otlp.proxy.*`      | OTLP HTTP proxy (`host`, `port`)                               |
| `rose.mybatis-plus.observation.*`      | SQL observation (MyBatis interceptor)                          |
| `rose.multitenancy.*`                  | Multitenancy resolution, MDC logging, async propagation        |
| `rose.dev.enabled`                     | Global dev services toggle                                     |
| `rose.dev.<connector>.*`               | Per-connector dev service settings                             |

Default Boot auto-configuration exclusions ship in `META-INF/config/default/*.properties` from
`rose-spring-boot-core`; baseline auto-configuration lives in `rose-spring-boot-autoconfigure`, and
applications should depend on `rose-spring-boot-starter`.

### Spring Boot module layout

| Artifact                                  | Layer            | Purpose                                                                  | Typical consumer |
|-------------------------------------------|------------------|--------------------------------------------------------------------------|------------------|
| `rose-spring-boot-core`                   | runtime          | Boot startup SPI, default config merge, diagnostics, shared task support | Rarely direct    |
| `rose-spring-boot-autoconfigure`          | autoconfigure    | Baseline Rose Boot auto-configuration                                    | Rarely direct    |
| `rose-spring-boot-starter`                | starter          | Recommended baseline starter for applications                            | Yes              |
| `rose-actuator-spring-boot-autoconfigure` | autoconfigure    | Rose actuator-specific auto-configuration                                | Rarely direct    |
| `rose-actuator-spring-boot-starter`       | starter          | Actuator starter layered on top of the baseline starter                  | Yes, when needed |

Recommended dependency shape:

- Most applications depend on `rose-spring-boot-starter`.
- Applications that need Rose actuator integration add `rose-actuator-spring-boot-starter`.
- `*-autoconfigure` modules are for manual composition or framework-internal aggregation.
- `rose-spring-boot-core` is infrastructure and should usually not be used as the application entrypoint.
- `RoseBinder` is an experimental helper for extension authors; `PropertyConstants` is internal module glue, not a consumer-facing API.

### Spring Boot bootstrap extensions

| Property                                     | Default | Module                          | Purpose                                                              |
|----------------------------------------------|---------|---------------------------------|----------------------------------------------------------------------|
| `rose.default-config.enabled`                | `true`  | `rose-spring-boot-core`         | Merge `config/default/*` and `META-INF/config/default/*` into Boot defaults |
| `rose.default-config.locations`              | empty   | `rose-spring-boot-core`         | Append extra classpath patterns to the default config merge scan     |
| `rose.autoconfigure.exclude`                 | empty   | `rose-spring-boot-autoconfigure` | Accumulate additional Boot auto-configuration exclusions across property sources |
| `rose.diagnostics.artifacts-collision.enabled` | `false` | `rose-spring-boot-core`       | Detect duplicate Maven coordinates on startup and fail fast          |

`rose.autoconfigure.exclude` is intentionally different from `spring.autoconfigure.exclude`: Rose accumulates
values from multiple `config/default/*` resources instead of letting later sources overwrite earlier ones.

---

## Design Documents

| Document                                                                                                               | Topic                  |
|------------------------------------------------------------------------------------------------------------------------|------------------------|
| [rose-spring-property-source-design.md](../../docs/design/rose-spring-property-source-design.md)                       | Property sources       |
| [rose-spring-env-refresh-design.md](../../docs/design/rose-spring-env-refresh-design.md)                               | Environment refresh    |
| [rose-spring-boot-bootstrap-diagnostics-design.md](../../docs/design/rose-spring-boot-bootstrap-diagnostics-design.md) | Bootstrap diagnostics  |
| [rose-spring-web-handler-design.md](../../docs/design/rose-spring-web-handler-design.md)                               | Spring Web handler SPI |
| [rose-i18n-design.md](../../docs/design/rose-i18n-design.md)                                                           | i18n (planned)         |
| [rose-cache-design.md](../../docs/design/rose-cache-design.md)                                                         | Cache (planned)        |
