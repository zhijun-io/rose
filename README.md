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
- **Composable starters** — add only observation, multitenancy, MyBatis-Plus, or baseline bootstrap as needed.
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
            <version>0.0.1-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-spring-boot-core</artifactId>
    </dependency>
</dependencies>
```

Use released versions from [Maven Central](https://central.sonatype.com/) when available; `${revision}` in this repository tracks the current SNAPSHOT.

## Starters

Pick one or combine several. Feature starters already include `rose-spring-boot-core` transitively—do not declare it again.

| Starter | Use when |
|---------|----------|
| `rose-spring-boot-core` | Baseline Rose platform (`RoseBinder`, shared Boot utilities) |
| `rose-observation-spring-boot` | Full OTel stack: SDK autoconfig, logs, OTLP metrics, semantic conventions, conventions selection, Actuator |
| `rose-observation-spring-boot-otel` | OTel SDK Boot auto-configuration only (compose with bridge slices as needed) |
| `rose-multitenancy-spring-boot` | Multitenancy Boot auto-configuration (includes HTTP tenant resolution on servlet apps) |
| `rose-mybatis-plus-spring-boot` | MyBatis-Plus audit, encryption, data permission |

```xml
<!-- Observation -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-observation-spring-boot</artifactId>
</dependency>

<!-- Multitenancy -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot</artifactId>
</dependency>
```

## Features

### Dev services

Docker-backed dev services for development. Add `rose-spring-boot-core`, optional `rose-devservice-spring-boot-*` connectors (`runtime` + `optional`), and the matching Spring Boot starter (e.g. JDBC):

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-devservice-spring-boot-postgresql</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

Connectors include PostgreSQL, MySQL, Redis, MongoDB, Kafka, RabbitMQ, Artemis, ActiveMQ, Ollama, MQTT, OpenLit, and OpenTelemetry Collector. Defaults use username/password (and JDBC database name) **`rose`** unless overridden via `rose.dev.<connector>.*`.

**Production:** dev services are **off by default**. Enable explicitly with `rose.dev.enabled=true` and/or `rose.dev.<connector>.enabled=true`. In local **DEV** or **TEST** bootstrap mode, connectors activate unless disabled. See [rose-devservice/README.md](rose-devservice/README.md).

### OpenTelemetry metrics

| Module | Path | When |
|--------|------|------|
| `rose-observation-spring-boot-micrometer-otlp` | Micrometer → OTLP | Default in the OTel starter |
| `rose-observation-spring-boot-micrometer-bridge` | Micrometer → OTel SDK | Metrics share the same SDK as traces/logs |

See [rose-observation/README.md](rose-observation/README.md).

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
| Module layering & naming | [docs/rose-module-layering.md](docs/rose-module-layering.md) |

**Module READMEs:** [rose-build](rose-build/) · [rose-bom](rose-bom/) · [rose-core](rose-core/) · [rose-spring](rose-spring/) · [rose-spring-boot](rose-spring-boot/) · [rose-mybatis-plus](rose-mybatis-plus/) · [rose-observation](rose-observation/) · [rose-multitenancy](rose-multitenancy/) · [rose-devservice](rose-devservice/) · [rose-coverage](rose-coverage/)

The [rose-bom](rose-bom/) manages **30+** published `io.zhijun` coordinates (starters, core libraries, dev-service connectors). `rose-spring-web` / `rose-spring-boot-web` are layout placeholders today — see [docs/rose-module-layering.md](docs/rose-module-layering.md).

Build layout follows [microsphere-build](https://github.com/microsphere-projects/microsphere-build).

**Help:** open a [GitHub issue](https://github.com/zhijun-io/rose/issues) for bugs and feature requests.

## Contributing

Rose follows Conventional Commits (`feat:`, `fix:`, `docs:`, …), one logical change per PR, and integration tests (`*IT`) for new dev-service connectors. Dependency bumps are handled by Renovate (`renovate.json`).

Build and release: [rose-build/README.md](rose-build/README.md). Changelog: [CHANGELOG.md](CHANGELOG.md).

## Reference

### Platform baseline & support

Rose **0.0.x** targets **Spring Boot 2.7.x** and **Java 8+**. Spring Boot 2.7 [reached end of open-source support](https://spring.io/projects/spring-boot#support) in November 2023; patch releases may still arrive via the BOM, but **new applications should plan a Boot 3 / Java 17 migration**. Renovate is capped at Boot `<3.0.0` until a Rose 1.x line ships — track [CHANGELOG.md](CHANGELOG.md) and GitHub issues for migration work.

### Bill of Materials

- Applications **import** [rose-bom](rose-bom/) in `dependencyManagement`; they do **not** inherit `rose-parent` or `rose-build`.
- New published coordinates are added to **`rose-bom` only**; `rose-parent` imports that BOM for the reactor build.

### Versioning

Single source: `rose-build/pom.xml` → `<revision>…</revision>`. All modules use `${revision}`; `flatten-maven-plugin` resolves CI-friendly versions on install/deploy.

### Build (this repository)

**Requirements:** JDK 8, Maven 3.6+, Docker for integration tests and dev services.

```bash
mvn validate
mvn test                 # unit test (*Test / *Tests)
mvn verify               # unit + integration (*IT; needs Docker; no JaCoCo)
mvn verify -DskipITs     # verify lifecycle without ITs
mvn verify -Pcoverage    # optional: JaCoCo (CI JDK 21); open <module>/target/site/jacoco/index.html
```

Publish locally: `mvn -B clean deploy -Prelease` (Central Portal token + GPG; see [rose-build/README.md](rose-build/README.md)).

### License

[Apache License 2.0](LICENSE).
