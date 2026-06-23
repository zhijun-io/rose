# Consumer Guide

How to consume Rose artifacts in a Spring Boot 2.7 application.

---

## Dependency Model

| Layer | Artifact | Application usage |
|---|---|---|
| BOM | `rose-bom` | `dependencyManagement` import |
| Baseline | `rose-spring-boot-core` | Default platform starter |
| Feature | `rose-*-spring-boot` | Observability, multitenancy, MyBatis-Plus |
| Dev services | `rose-devservice-spring-boot-*` | Optional `runtime` connectors |
| Libraries | `rose-*-core`, `rose-multitenancy-spring` | Advanced / manual wiring |

Applications inherit **`spring-boot-starter-parent`** (or a corporate parent). They do **not** inherit `rose-parent` or `rose-build`.

---

## Starter Matrix

| Starter | Use when |
|---|---|
| `rose-spring-boot-core` | Baseline Rose platform (`RoseBinder`, shared Boot utilities) |
| `rose-opentelemetry-spring-boot` | OTel SDK, logs, OTLP metrics, semantic conventions |
| `rose-multitenancy-spring-boot` | Multitenancy Boot auto-configuration (HTTP resolution on servlet apps) |
| `rose-mybatis-plus-spring-boot` | MyBatis-Plus audit, encryption, data permission |

```xml
<!-- Observability -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-opentelemetry-spring-boot</artifactId>
</dependency>

<!-- Multitenancy -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-multitenancy-spring-boot</artifactId>
</dependency>
```

---

## Configuration Prefixes

| Area | Prefix | Example |
|---|---|---|
| OpenTelemetry | `rose.otel.*` | `rose.otel.enabled=true` |
| Multitenancy | `rose.multitenancy.*` | tenant resolver, MDC key |
| Dev services (global) | `rose.dev.enabled` | `rose.dev.enabled=true` |
| Dev services (connector) | `rose.dev.<connector>.*` | `rose.dev.postgresql.enabled=true` |
| Bootstrap profiles | `rose.bootstrap.*`, `rose.dev.profiles` | dev/test profile activation |

OTel standard environment variables (`OTEL_*`) are mapped to `rose.otel.*` via environment post-processors. See [rose-opentelemetry/README.md](../rose-opentelemetry/README.md).

---

## Dev Service Connectors

Available connectors (each is a separate artifact):

`postgresql`, `mysql`, `redis`, `mongodb`, `kafka`, `rabbitmq`, `artemis`, `activemq`, `ollama`, `mqtt`, `openlit`, `otel-collector`

Default credentials and database name are **`rose`** unless overridden.

---

## Module README Index

| Module | README |
|---|---|
| BOM | [rose-bom/README.md](../rose-bom/README.md) |
| Build | [rose-build/README.md](../rose-build/README.md) |
| Core | [rose-core/README.md](../rose-core/README.md) |
| Spring | [rose-spring/README.md](../rose-spring/README.md) |
| Spring Boot | [rose-spring-boot/README.md](../rose-spring-boot/README.md) |
| OpenTelemetry | [rose-opentelemetry/README.md](../rose-opentelemetry/README.md) |
| Multitenancy | [rose-multitenancy/README.md](../rose-multitenancy/README.md) |
| Dev services | [rose-devservice/README.md](../rose-devservice/README.md) |
| MyBatis-Plus | [rose-mybatis-plus/README.md](../rose-mybatis-plus/README.md) |
| Observation | [rose-observation/README.md](../rose-observation/README.md) |

---

## Publishing Your Application

Rose library artifacts are published to Maven Central under `io.zhijun`. Your application uses the BOM import pattern above — no need to inherit `rose-build`.

For releasing Rose itself, see [CI/CD Integration](CI-CD-Integration).
