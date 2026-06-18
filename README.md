# Rose

<p align="center">
  <a href="https://github.com/zhijun-io/rose/actions/workflows/maven-ci.yml?query=branch%3Amain"><img src="https://img.shields.io/github/actions/workflow/status/zhijun-io/rose/maven-ci.yml?branch=main&logo=GitHub&label=Build" alt="Build" /></a>
  <a href="https://scorecard.dev/viewer/?uri=github.com/zhijun-io/rose"><img src="https://api.scorecard.dev/projects/github.com/zhijun-io/rose/badge" alt="OpenSSF Scorecard" /></a>
  <img src="https://img.shields.io/badge/Java-8-orange?logo=openjdk" alt="Java 8" />
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-2.7-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 2.7" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="Apache 2.0 License" /></a>
</p>

Rose is a **Spring Boot 2.7 / Java 8 extension platform**: optional starters for bootstrap, OpenTelemetry, multitenancy, dev services, and related libraries. It extends Spring Boot; it does not replace or mirror the Spring Boot project.

## Contents

- [Quick start](#quick-start)
- [Starters](#starters)
- [Features](#features)
  - [Dev Services](#dev-services)
  - [OpenTelemetry metrics](#opentelemetry-metrics)
- [Reference](#reference)
  - [Bill of Materials](#bill-of-materials)
  - [Versioning](#versioning)
  - [License](#license)
- [Repository](#repository)
  - [Module layout](#module-layout)
  - [Development principles](#development-principles)
  - [Build](#build)
  - [Contributing](#contributing)

## Quick start

Use Spring Boot as the application parent and import Rose versions via BOM:

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

Add **feature starters** when you need more than Rose bootstrap (see [Starters](#starters)). You do not declare `rose-spring-boot-starter` again — it is already transitive.

## Starters

Starters are layered aggregates. Pick one or combine several.

| Starter | When to use |
|---------|-------------|
| `rose-spring-boot-starter` | Rose bootstrap, dev/test profiles, baseline platform |
| `rose-opentelemetry-spring-boot-starter` | OpenTelemetry SDK, logs, OTLP metrics, semantic conventions, Actuator |
| `rose-multitenancy-core-spring-boot-starter` | Multitenancy without web stack |
| `rose-multitenancy-web-spring-boot-starter` | Multitenancy + `spring-boot-starter-web` |
| `rose-mybatis-plus-spring-boot-starter` | MyBatis-Plus audit, encryption, data permission |

**Stacking**

```
rose-spring-boot-starter
├── rose-opentelemetry-spring-boot-starter
└── rose-multitenancy-core-spring-boot-starter
    └── rose-multitenancy-web-spring-boot-starter
```

**Examples**

```xml
<!-- Observability only -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-opentelemetry-spring-boot-starter</artifactId>
</dependency>

<!-- Multitenancy on the web stack -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-web-spring-boot-starter</artifactId>
</dependency>
```

Dev service connectors are **not** starters — add them as optional `runtime` dependencies ([Dev Services](#dev-services)).

## Features

### Dev Services

Local Docker-backed services for development. Requires a container runtime at runtime (OrbStack, Docker Desktop, etc.).

Add `rose-spring-boot-starter` plus the connectors you need and the matching Spring Boot starter for the technology:

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-dev-services-postgresql</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

**Connectors** (under `rose-dev-services/`): `postgresql`, `mysql`, `redis`, `mongodb`, `kafka`, `rabbitmq`, `artemis`, `activemq`, `ollama`, `mqtt`, `openlit`, `otel-collector`, and others.

JDBC and messaging connectors default to username/password (and JDBC database name) **`rose`** unless overridden via `rose.dev.services.<connector>.*`.

### OpenTelemetry metrics

Two Micrometer paths — choose based on whether metrics should use the OpenTelemetry SDK or export directly via OTLP.

| Module | Path | When to use |
|--------|------|-------------|
| `rose-opentelemetry-micrometer-registry-otlp` | Micrometer → OTLP | **Default** in `rose-opentelemetry-spring-boot-starter`; Actuator metrics over OTLP without joining the SDK meter pipeline |
| `rose-opentelemetry-micrometer-metrics-bridge` | Micrometer → OTel SDK `MeterProvider` | Metrics must share the same `OpenTelemetrySdk` as traces and logs |

The OTel starter includes the OTLP registry. Add the metrics bridge explicitly for SDK-unified metrics:

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-opentelemetry-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-opentelemetry-micrometer-metrics-bridge</artifactId>
</dependency>
```

## Reference

### Bill of Materials

`rose-bom` aligns every published `io.zhijun` library and starter version. Import it in `dependencyManagement` (see [Quick start](#quick-start)).

**Contract**

- Applications **keep** `spring-boot-starter-parent` (or their own corporate parent) and **import** `rose-bom`.
- Applications **do not** inherit `rose-parent`; it is the Rose reactor aggregator.
- Applications **do not** inherit `rose-build`; it is the shared build parent for Rose modules.
- New published Rose artifacts are added to both `rose-parent` and `rose-bom` so consumers keep a single version line.

**Managed coordinates**

- Base: `rose-core`, `rose-spring-core`, `rose-spring-boot`, `rose-spring-boot-starter`
- Data and persistence: `rose-excel`, `rose-sqlite`, `rose-mybatis-plus-core`, `rose-mybatis-plus-spring-boot-starter`
- Observability: `rose-observation-core`, `rose-opentelemetry-*`
- Multitenancy: `rose-multitenancy-*`
- Dev Services: `rose-dev-services-*`

When artifact names change, treat `rose-bom` as the primary migration contract and document the old-to-new coordinates in release notes or upgrade docs.

### Versioning

Bump the release version in one place:

```xml
<!-- rose-build/pom.xml -->
<revision>0.0.0.2-SNAPSHOT</revision>
```

All modules inherit `${revision}`. `flatten-maven-plugin` resolves CI-friendly versions on build/install.

To publish to Maven Central, use the `release` profile (`mvn deploy -Prelease`). See [docs/releasing.md](docs/releasing.md) and [CONTRIBUTING.md](CONTRIBUTING.md#releasing-to-maven-central-sonatype).

### License

[Apache License 2.0](LICENSE).

## Repository

### Module layout

Rose is a **library platform** organized in four layers:

```
Build          rose-build, rose-parent, rose-bom
  │
Base           rose-core → rose-spring-core → rose-spring-boot
  │
Capabilities   rose-opentelemetry/*, rose-multitenancy/*, rose-observation/*,
               rose-excel, rose-sqlite, rose-mybatis-plus/*, rose-dev-services/*
  │
Starters       rose-*-spring-boot-starter
```

| Layer | Key artifacts | Role |
|-------|---------------|------|
| Build | `rose-build`, `rose-parent`, `rose-bom` | Shared build parent, dependency/version parent, consumer version alignment |
| Base | `rose-core`, `rose-spring-core`, `rose-spring-boot` | 工具库；Spring 扩展（环境监听、增强属性源）；Rose bootstrap 与 dev-mode profiles |
| Capabilities | `rose-{domain}/*` | Feature libraries; auto-configuration inside each JAR |
| Starters | `rose-*-spring-boot-starter` | Thin POMs; what applications depend on |

**Design principle:** capability modules stay **library-thin** (fine-grained Spring dependencies); starters own the **runnable stack** (`spring-boot-starter-web`, Actuator, JDBC pool, etc.).

**Normative specification for contributors:** [docs/module-layering.md](docs/module-layering.md) — required for new modules and dependency changes (checklists, dependency rules, reactor tree).

### Development principles

Design and review guidance: [docs/development-principles.md](docs/development-principles.md) — do not reinvent the wheel; extend Spring Boot; compose existing Rose modules; minimal correct changes.

### Build

**Requirements:** JDK 8, Maven 3.6+, Docker for dev services and integration tests.

```bash
mvn validate
mvn compile test-compile    # compile only, no Docker
mvn test                    # integration tests need Docker
mvn verify -Pcoverage       # same as CI; JaCoCo aggregate + 90% line check

mvn -B -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=zhijun-io \
  -Dsonar.projectKey=rose \
  -DskipTests \
  -Dsonar.coverage.jacoco.xmlReportPaths=rose-coverage/target/site/jacoco-aggregate/jacoco.xml
```

Integration tests use Testcontainers 1.21.4 (Docker API 1.44+). Dev Services auto-detect OrbStack (`~/.orbstack/run/docker.sock`) or `/var/run/docker.sock`. Override if needed:

```bash
export DOCKER_HOST=unix://$HOME/.orbstack/run/docker.sock
```

First integration test run may be slow while images are pulled.

**Build governance**

- `rose-build` owns plugin versions, coverage policy, publishing setup, and shared build-environment enforcement.
- `rose-parent` owns the reactor module list, dependency/version management, and aggregation profiles used to build the whole repository.
- `rose-bom` is the consumer-facing dependency contract for applications using Rose artifacts.
- Feature modules should not duplicate build or version policy locally unless there is a documented exception.

### Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). New modules must follow [docs/module-layering.md](docs/module-layering.md) and [docs/development-principles.md](docs/development-principles.md).
