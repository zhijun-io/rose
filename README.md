# Rose

<p align="center">
  <a href="https://github.com/zhijun-io/rose/actions/workflows/maven-build.yml?query=branch%3Amain"><img src="https://img.shields.io/github/actions/workflow/status/zhijun-io/rose/maven-build.yml?branch=main&logo=GitHub&label=Build" alt="Build" /></a>
  <a href="https://app.codecov.io/gh/zhijun-io/rose"><img src="https://codecov.io/gh/zhijun-io/rose/branch/main/graph/badge.svg" alt="Codecov" /></a>
  <img src="https://img.shields.io/maven-central/v/io.github.zhijun-io/rose.svg" alt="Maven" />
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="Apache 2.0 License" /></a>
</p>

Rose is a **Spring Boot 2.7 / Java 8 extension platform** (`io.zhijun`): optional starters and libraries for bootstrap utilities, OpenTelemetry, multitenancy, dev services, and related capabilities. Rose extends Spring Boot—it does not replace it.

## Why Rose

- **BOM-aligned versions** — import `rose-bom` once; all `io.zhijun` artifacts stay in sync.
- **Composable starters** — add only observability, multitenancy, MyBatis-Plus, or baseline bootstrap as needed.
- **Local dev services** — Docker-backed connectors (PostgreSQL, Redis, Kafka, …) with sensible defaults and dynamic properties.
- **OpenTelemetry-first** — tracing, logs, OTLP metrics, and optional Micrometer ↔ SDK bridges.
- **Library-thin modules** — capability JARs stay small; starters own the runnable stack (Web, JDBC, Actuator).

## Getting started

**Requirements:** Java 8+, Spring Boot 2.7.x application.

Keep `spring-boot-starter-parent` (or your corporate parent), import the Rose BOM, and add a starter:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.zhijun</groupId>
            <artifactId>rose-bom</artifactId>
            <version>0.0.0.2-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

Use released versions from [Maven Central](https://central.sonatype.com/) when available; `${revision}` in this repository tracks the current SNAPSHOT.

## Starters

Pick one or combine several. Feature starters already include `rose-spring-boot-starter` transitively—do not declare it again.

| Starter | Use when |
|---------|----------|
| `rose-spring-boot-starter` | Baseline Rose platform (`RoseBinder`, shared Boot utilities) |
| `rose-opentelemetry-spring-boot-starter` | OTel SDK, logs, OTLP metrics, semantic conventions, Actuator |
| `rose-multitenancy-spring-boot-starter` | Multitenancy Boot auto-configuration |
| `rose-multitenancy-web` | Web integration (add alongside starter for HTTP tenant resolution) |
| `rose-mybatis-plus-spring-boot-starter` | MyBatis-Plus audit, encryption, data permission |

```xml
<!-- Observability -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-opentelemetry-spring-boot-starter</artifactId>
</dependency>

<!-- Multitenancy -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-web</artifactId>
</dependency>
```

## Features

### Local services

Docker-backed local services for development. Add `rose-spring-boot-starter`, optional `rose-devservice-*` connectors (`runtime` + `optional`), and the matching Spring Boot starter (e.g. JDBC):

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-devservice-postgresql</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

Connectors include PostgreSQL, MySQL, Redis, MongoDB, Kafka, RabbitMQ, Artemis, ActiveMQ, Ollama, MQTT, OpenLit, and OpenTelemetry Collector. Defaults use username/password (and JDBC database name) **`rose`** unless overridden via `rose.dev.<connector>.*`.

Global toggle: `rose.dev.enabled`. See [rose-devservice/README.md](rose-devservice/README.md).

### OpenTelemetry metrics

| Module | Path | When |
|--------|------|------|
| `rose-opentelemetry-micrometer-registry-otlp` | Micrometer → OTLP | Default in the OTel starter |
| `rose-opentelemetry-micrometer-metrics-bridge` | Micrometer → OTel SDK | Metrics share the same SDK as traces/logs |

See [rose-opentelemetry/README.md](rose-opentelemetry/README.md).

## Documentation

| Topic | Location |
|-------|----------|
| **Wiki (guides)** | [`wiki/`](wiki/) — synced to [GitHub Wiki](https://github.com/zhijun-io/rose/wiki) on push |
| Getting started | [wiki/Getting-Started.md](wiki/Getting-Started.md) |
| Consumer guide | [wiki/Consumer-Guide.md](wiki/Consumer-Guide.md) |
| Build profiles | [wiki/Profiles-Management.md](wiki/Profiles-Management.md) |
| BOM & consumer contract | [rose-bom/README.md](rose-bom/README.md) |
| Build, CI, release | [rose-build/README.md](rose-build/README.md) |
| Design notes | [docs/](docs/) |

**Module READMEs:** [rose-build](rose-build/) · [rose-bom](rose-bom/) · [rose-core](rose-core/) · [rose-spring](rose-spring/) · [rose-spring-boot](rose-spring-boot/) · [rose-mybatis-plus](rose-mybatis-plus/) · [rose-observation](rose-observation/) · [rose-opentelemetry](rose-opentelemetry/) · [rose-multitenancy](rose-multitenancy/) · [rose-devservice](rose-devservice/)

Build layout follows [microsphere-build](https://github.com/microsphere-projects/microsphere-build).

**Help:** open a [GitHub issue](https://github.com/zhijun-io/rose/issues) for bugs and feature requests.

## Contributing

Rose follows Conventional Commits (`feat:`, `fix:`, `docs:`, …), one logical change per PR, and integration tests (`*IT`) for new dev-service connectors. Dependency bumps are handled by Renovate (`renovate.json`).

Build and release details: [rose-build/README.md](rose-build/README.md). Changelog: [CHANGELOG.md](CHANGELOG.md).

## Reference

### Bill of Materials

- Applications **import** [rose-bom](rose-bom/) in `dependencyManagement`; they do **not** inherit `rose-parent` or `rose-build`.
- New published coordinates are added to **`rose-bom` only**; `rose-parent` imports that BOM for the reactor build.

### Versioning

Single source: `rose-build/pom.xml` → `<revision>…</revision>`. All modules use `${revision}`; `flatten-maven-plugin` resolves CI-friendly versions on install/deploy.

### Build (this repository)

**Requirements:** JDK 8, Maven 3.6+, Docker for integration tests and dev services.

```bash
mvn validate
mvn test                 # unit tests (*Test / *Tests)
mvn verify               # unit + integration (*IT; needs Docker; no JaCoCo)
mvn verify -DskipITs     # verify lifecycle without ITs
mvn verify -Pcoverage    # optional: JaCoCo (CI JDK 21); open <module>/target/site/jacoco/index.html
```

Publish locally: `mvn -B clean deploy -Prelease` (Central Portal token + GPG; see [rose-build/README.md](rose-build/README.md)).

### License

[Apache License 2.0](LICENSE).
