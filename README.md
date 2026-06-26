# Rose

[![CI](https://github.com/zhijun-io/rose/actions/workflows/ci.yml/badge.svg)](https://github.com/zhijun-io/rose/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/zhijun-io/rose)](LICENSE)

Spring Boot 2.7 / Java 8 extension libraries and starters for `io.zhijun` — bootstrap utilities, OpenTelemetry, multitenancy, MyBatis-Plus, and dev services. Import **`rose-bom`** in your application; do not inherit `rose-build` or `rose-parent`.

## Introduction

Rose gives you composable starters with BOM-aligned versions, optional Testcontainers-based dev services, and shared build governance. User guides, feature overview, and copy-paste Maven setup live in the wiki — linked below.

## Packages

| Path | Purpose |
| --- | --- |
| `rose-bom/` | Consumer BOM |
| `rose-foundation/` | Annotations, core utilities, test support |
| `rose-spring-boot/` | Bootstrap and baseline Boot starters |
| `rose-observation/` | Tracing, logs, OTLP, Micrometer bridges |
| `rose-multitenancy/` | Multitenancy support |
| `rose-mybatis-plus/` | MyBatis-Plus integration |
| `rose-devservice/` | Dev-service connectors (`rose-devservice-spring-boot-*`) |

Full reactor list: root `pom.xml`. BOM artifact list: [wiki/rose-bom/Consumer-Guide.md](wiki/rose-bom/Consumer-Guide.md).

## Documentation

| Topic | Link |
| --- | --- |
| Overview and quick start | [wiki/Home.md](wiki/Home.md) |
| Getting started | [wiki/rose-spring-boot/Getting-Started.md](wiki/rose-spring-boot/Getting-Started.md) |
| Configuration reference | [wiki/rose-spring-boot/Configuration-Reference.md](wiki/rose-spring-boot/Configuration-Reference.md) |
| Consumer BOM guide | [wiki/rose-bom/Consumer-Guide.md](wiki/rose-bom/Consumer-Guide.md) |
| Build profiles and CI | [wiki/rose-build/Profiles-Management.md](wiki/rose-build/Profiles-Management.md) · [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md) |
| Design specs | [docs/design/README.md](docs/design/README.md) |
| Implementation rules (中文) | [docs/rose-conventions.md](docs/rose-conventions.md) |

Wiki sources under `wiki/` sync to [GitHub Wiki](https://github.com/zhijun-io/rose/wiki) on push to `main` (`.github/workflows/wiki.yml`).

## Getting Started

Import `rose-bom`, then add the starters you need — see [wiki/Home.md](wiki/Home.md) for a minimal Maven example and [wiki/rose-spring-boot/Getting-Started.md](wiki/rose-spring-boot/Getting-Started.md) for setup details.

## Development

Clone this repository to work on Rose itself (consumers typically depend on published artifacts only). Local JDK: `.sdkmanrc` — run `sdk env` when using SDKMAN. Build and verify commands: [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md#local-ci-parity).

## Contributing

See [docs/rose-conventions.md](docs/rose-conventions.md) (中文). Open issues on [GitHub Issues](https://github.com/zhijun-io/rose/issues).

## License

[Apache License 2.0](LICENSE)
