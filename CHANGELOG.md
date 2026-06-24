# Changelog

All notable changes to Rose (`io.zhijun`) are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Multitenancy modules: auto-configuration moved to `rose-multitenancy-spring-boot`; `rose-multitenancy-spring` and `rose-multitenancy-core` no longer depend on Spring Boot.
- Dev Services: artifacts renamed to `rose-devservice-spring-boot-{tech}` (replaces `rose-devservice-{tech}`). Configuration keys remain `rose.dev.*`.
- Dev Services: Java packages renamed from `io.zhijun.dev.*` to `io.zhijun.devservice.*` (breaking for direct imports).
- Dev Services: class prefix renamed from `LocalService*` to `DevService*`.
- Observation: merged `rose-observation` and `rose-opentelemetry` into `rose-observation`; conventions use `TelemetryConventionsBackend` with `rose.observation.conventions.backend` selection; default stack is `rose-observation-spring-boot`.
- Observation: Boot auto-configuration moved to `rose-observation-spring-boot`; use `rose-observation-spring-boot` for full stack.
- OpenTelemetry: SDK moved to `rose-observation-spring-boot-otel`; packages under `io.zhijun.observation.boot.autoconfigure.otel.*`.
- **Package layout (breaking)**: business domains mirror Maven modules — `{domain}.core.*` (`*-core`), `{domain}.spring.*` (`*-spring`), `{domain}.boot.autoconfigure[.{slice}].*` (`*-spring-boot`); DevService registration at `{domain}.boot.registration.*`. See `rose-bom/README.md`.
- Removed unused BOM entries `rose-excel` and `rose-sqlite`.

### Migration

| Before | After |
|--------|-------|
| `io.zhijun.dev.*` | `io.zhijun.devservice.*` |
| `io.zhijun.opentelemetry.*` | `io.zhijun.observation.boot.autoconfigure.otel.*` |
| `io.zhijun.observation.*` | `io.zhijun.observation.*` / `io.zhijun.observation.boot.autoconfigure.*` |
| `rose-observation-*` / `rose-opentelemetry-*` | `rose-observation-*`（见 README） |
| `rose-opentelemetry-core` | `rose-observation-spring-boot` |
| `rose.observations.conventions.*` | `rose.observation.conventions.*` |
| `io.zhijun.multitenancy.web.*` | `io.zhijun.multitenancy.spring.web.*` |
| `io.zhijun.multitenancy.autoconfigure.*` | `io.zhijun.multitenancy.boot.autoconfigure.*` |
| `io.zhijun.devservice.core.registration.*` / `io.zhijun.devservice.autoconfigure.*` | `io.zhijun.devservice.boot.registration.*` / `io.zhijun.devservice.boot.autoconfigure.*` |
| `io.zhijun.devservice.{tech}.*` | `io.zhijun.devservice.boot.autoconfigure.{tech}.*` |
| `io.zhijun.observation.autoconfigure.*` | `io.zhijun.observation.boot.autoconfigure.*` |
| `io.zhijun.mybatisplus.core.autoconfigure.*` | `io.zhijun.mybatisplus.boot.autoconfigure.*` |

## [0.1.0] - Unreleased

First public release of Rose — a Spring Boot 2.7 / Java 8 extension platform (`io.zhijun`).

### Added

#### Platform

- Maven reactor with `${revision}` versioning and `flatten-maven-plugin`
- `rose-bom` for aligned dependency management across all published artifacts
- `rose-core` utilities (`PropertyAdapter`, incubating/internal markers)
- `rose-spring-boot` 父模块（`rose-spring-boot-core`、`rose-spring-boot-starter`）；使用 Spring profiles 区分环境
- `rose-observation-core` observation support
- JaCoCo aggregate coverage via `rose-coverage` (`mvn verify -Pcoverage`)

#### Spring Boot starters

- `rose-spring-boot-starter` — baseline Rose platform
- `rose-observation-spring-boot-otel-starter` — OpenTelemetry SDK, logs, OTLP metrics, Actuator
- `rose-multitenancy-core-spring-boot-starter` — multitenancy without the web stack
- `rose-multitenancy-web-spring-boot-starter` — multitenancy with `spring-boot-starter-web`

#### OpenTelemetry

- Auto-configuration for tracing, metrics, logs, and resource contributors
- `rose-observation-spring-boot-logback` — Logback ↔ OpenTelemetry logs
- `rose-observation-spring-boot-micrometer-otlp` — Micrometer → OTLP metrics (default in OTel starter)
- `rose-observation-spring-boot-micrometer-bridge` — Micrometer → OpenTelemetry `MeterProvider`
- `rose-observation-spring-boot-conventions-otel` — Rose semantic conventions

#### Dev Services

- Docker-backed local dev services (`rose-devservice-core`, Testcontainers-based connectors)
- Connectors: PostgreSQL, MySQL, Redis, MongoDB, Kafka, RabbitMQ, Artemis, ActiveMQ, MQTT (HiveMQ), Ollama, OpenLit, OpenTelemetry Collector
- Default credentials / JDBC database name `rose` unless overridden via `rose.dev.*`
- Actuator endpoint and dynamic property registration for running containers

#### Multitenancy

- `rose-multitenancy-core` and `rose-multitenancy-web` (`rose.multitenancy.*`)

#### Build & release

- Maven `release` profile: sources, Javadoc, GPG signing, Central Publisher Portal deploy
- GitHub Actions: Maven CI, snapshot publish, Central release (via [zhijun-io/workflows](https://github.com/zhijun-io/workflows))
- OpenSSF Scorecard and CodeQL workflows

#### Documentation

- `README.md`, `CONTRIBUTING.md`, `docs/development-principles.md`, `docs/module-layering.md`, `docs/releasing.md`

### Notes

- Applications should use `spring-boot-starter-parent` and **import** `rose-bom`; they do not inherit `rose-parent`.
- Auto-configuration is registered via `META-INF/spring.factories` (Spring Boot 2.7).

[Unreleased]: https://github.com/zhijun-io/rose/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/zhijun-io/rose/releases/tag/v0.1.0
