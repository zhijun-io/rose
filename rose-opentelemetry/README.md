# Rose OpenTelemetry

OpenTelemetry SDK integration, log bridges, Micrometer OTLP export, and semantic conventions for Rose.

| Module | Artifact | Role |
|--------|----------|------|
| Core | `rose-opentelemetry-core` | SDK auto-configuration |
| Bridges | `rose-opentelemetry-logback-bridge`, `rose-opentelemetry-micrometer-*` | Logs and metrics |
| Conventions | `rose-opentelemetry-semantic-conventions` | Shared attributes |
| Starter | `rose-opentelemetry-spring-boot-starter` | Application entry point |

## Module defaults

Rose OTLP metrics use `rose-opentelemetry-micrometer-registry-otlp` instead of Spring Boot's built-in OTLP metrics export.

Modules ship merged exclusions via `rose/default/*.properties`:

| File | Excludes |
|------|----------|
| `rose-opentelemetry-core` → `opentelemetry.properties` | `OtlpMetricsExportAutoConfiguration` |
| `rose-opentelemetry-micrometer-registry-otlp` → `micrometer-registry-otlp.properties` | same (accumulates) |

Requires `rose-spring-boot-core` on the classpath for `rose/default/*` loading and `RoseAutoConfigurationImportFilter`. The starter (`rose-opentelemetry-spring-boot-starter`) includes it transitively via `rose-spring-boot-starter`.

`OtlpMetricsExportAutoConfiguration` exists from Spring Boot 3 onward; the exclusion is forward-compatible and harmless on Boot 2.7.
