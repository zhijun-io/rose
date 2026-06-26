# Consumer Guide

How to consume Rose artifacts in a Spring Boot 2.7 application.

---

## Dependency Model

| Layer | Artifact | Application usage |
|---|---|---|
| BOM | `rose-bom` | `dependencyManagement` import |
| Baseline | `rose-spring-boot-core` | Default platform starter |
| Feature | `rose-*-spring-boot` | Observation, multitenancy, MyBatis-Plus |
| Dev services | `rose-devservice-spring-boot-*` | Optional `runtime` connectors |
| Libraries | `rose-*-core`, `rose-multitenancy-spring` | Advanced / manual wiring |

Applications inherit **`spring-boot-starter-parent`** (or a corporate parent). They do **not** inherit `rose-parent` or `rose-build`.

---

## Starter Matrix

| Starter | Use when |
|---|---|
| `rose-spring-boot-core` | Baseline Rose platform (`RoseBinder`, shared Boot utilities) |
| `rose-observation-spring-boot` | Full OTel stack: SDK autoconfig, logs, OTLP metrics, semantic conventions |
| `rose-observation-spring-boot-otel` | OTel SDK Boot auto-configuration only |
| `rose-multitenancy-spring-boot` | Multitenancy Boot auto-configuration (HTTP resolution on servlet apps) |
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

---

## Configuration Prefixes

| Area | Prefix | Example |
|---|---|---|
| OpenTelemetry | `rose.otel.*` | `rose.otel.enabled=true` |
| Multitenancy | `rose.multitenancy.*` | tenant resolver, MDC key |
| Dev services (global) | `rose.dev.enabled` | `rose.dev.enabled=true` |
| Dev services (connector) | `rose.dev.<connector>.*` | `rose.dev.postgresql.enabled=true` |
| Bootstrap profiles | `rose.bootstrap.*`, `rose.dev.profiles` | dev/test profile activation |

OTel standard environment variables (`OTEL_*`) are mapped to `rose.otel.*` via environment post-processors. Prefixes: `rose.observation.*`, `rose.otel.*`.

---

## BOM artifacts

与 `rose-bom/pom.xml` 同步；新发布坐标**仅须加入 BOM**。命名规则见 [docs/rose-conventions.md](../../docs/rose-conventions.md)。

### Base

| Artifact | Description |
|----------|-------------|
| `rose-annotation` | Contract annotations (`io.zhijun.annotation`) |
| `rose-core` | Framework-agnostic utilities; transitively includes `rose-annotation` |
| `rose-test` | Cross-topic JUnit helpers (`test` scope) |
| `rose-annotation-processor` | Compile-time annotation processing (`optional`) |
| `rose-spring-core` | Spring extensions (property sources, env refresh, binding) |
| `rose-spring-boot-core` | Boot baseline (`RoseBinder`, `config/default/*`, diagnostics) |
| `rose-spring-boot-actuator` | Actuator extensions |

### Data & persistence

| Artifact | Description |
|----------|-------------|
| `rose-mybatis-plus-core` | Audit, encryption, data permission, tenant |
| `rose-mybatis-plus-spring` | Spring integration (no Boot auto-config) |
| `rose-mybatis-plus-spring-boot` | MyBatis-Plus Boot auto-configuration |

### Observation

| Artifact | Description |
|----------|-------------|
| `rose-observation-core` | `TelemetryConventionsBackend` API |
| `rose-observation-spring-boot` | Default OTel stack aggregator |
| `rose-observation-spring-boot-otel` | OTel SDK only |
| `rose-observation-spring-boot-logback` | Logback bridge |
| `rose-observation-spring-boot-micrometer-otlp` | Micrometer → OTLP |
| `rose-observation-spring-boot-micrometer-bridge` | Micrometer → OTel SDK |
| `rose-observation-spring-boot-conventions-otel` | Semantic conventions |

### Multitenancy

| Artifact | Description |
|----------|-------------|
| `rose-multitenancy-core` | Tenant context API |
| `rose-multitenancy-spring` | Spring + Servlet/WebMVC |
| `rose-multitenancy-spring-boot` | Boot auto-configuration |

### Dev services

| Artifact | Description |
|----------|-------------|
| `rose-devservice-core` | API, `BootstrapMode`, Testcontainers |
| `rose-devservice-spring-boot` | Boot auto-configuration and registration |
| `rose-devservice-test` | Shared IT support (`test` scope) |
| `rose-devservice-spring-boot-actuator` | `/actuator/devservices` |
| `rose-devservice-spring-boot-{postgresql,mysql,redis,mongodb,kafka,rabbitmq,artemis,activemq,ollama,mqtt,openlit,otel}` | Connectors (`runtime` + `optional`) |

### Not in BOM

| Artifact | Reason |
|----------|--------|
| `rose-build` / `rose-parent` | Build-only |
| `rose-spring-web` / `rose-spring-boot-web` | Incubating scaffolds |

---

## Documentation index

| Topic | Location |
|-------|----------|
| Contributing conventions | [docs/rose-conventions.md](../../docs/rose-conventions.md) |
| Design specs | [docs/design/](../../docs/design/) |
| Build & profiles | [Profiles-Management](../rose-build/Profiles-Management) |
| CI / release | [CI-CD-Integration](../rose-build/CI-CD-Integration) |

---

## Dev Service Connectors

Available connectors (each is a separate artifact):

`postgresql`, `mysql`, `redis`, `mongodb`, `kafka`, `rabbitmq`, `artemis`, `activemq`, `ollama`, `mqtt`, `openlit`, `otel`

Default credentials and database name are **`rose`** unless overridden. Production: `rose.dev.enabled=false` by default; override with `rose.dev.<connector>.*`.

---

## Publishing Your Application

Rose library artifacts are published to Maven Central under `io.zhijun`. Your application uses the BOM import pattern above — no need to inherit `rose-build`.

For releasing Rose itself, see [CI/CD Integration](../rose-build/CI-CD-Integration).
