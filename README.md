# Rose

[![CI](https://github.com/zhijun-io/rose/actions/workflows/ci.yml/badge.svg)](https://github.com/zhijun-io/rose/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/zhijun-io/rose)](LICENSE)
[![codecov](https://codecov.io/gh/zhijun-io/rose/graph/badge.svg)](https://codecov.io/gh/zhijun-io/rose)
![Java 8+](https://img.shields.io/badge/Java-8+-orange?logo=openjdk&logoColor=white)
![Spring Boot 2.7.18](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white)

**Rose** is a Spring Boot **2.7** / Java **8+** extension platform (`io.zhijun`) — composable starters and libraries for bootstrap utilities, OpenTelemetry observation, multitenancy, MyBatis-Plus, and Testcontainers-based dev services.

Applications import **`rose-bom`** for aligned versions. Do **not** use `rose-build` or `rose-parent` as your application parent.

## Why use Rose

- **BOM-aligned versions** — one `rose-bom` import keeps every `io.zhijun` artifact in sync
- **Composable starters** — observation, multitenancy, MyBatis-Plus, and a baseline bootstrap starter
- **Dev services** — optional Testcontainers connectors with dynamic properties (`rose-devservice-spring-boot-*`)
- **OpenTelemetry-first** — tracing, logs, OTLP metrics, and Micrometer bridges
- **Build governance** — shared `rose-build` enforcer, profiles, JaCoCo, and Maven Central publishing (this reactor only)

## Getting started

### Use Rose in your application

Keep `spring-boot-starter-parent` (or your own BOM) as parent; import Rose via BOM:

```xml
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
        <artifactId>rose-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

Use a released coordinate from [Maven Central](https://central.sonatype.com/) when published. Application entrypoints should prefer `rose-spring-boot-starter`; add `rose-actuator-spring-boot-starter` when actuator integration is needed. `rose-spring-boot-autoconfigure` is for advanced/manual composition. See [wiki/rose-bom/Consumer-Guide.md](wiki/rose-bom/Consumer-Guide.md).

Step-by-step walkthrough: [wiki/rose-spring-boot/Getting-Started.md](wiki/rose-spring-boot/Getting-Started.md).

### Build this repository

```bash
git clone https://github.com/zhijun-io/rose.git
cd rose
sdk env                         # optional — Java 8 (.sdkmanrc)
./mvnw -B -ntp validate              # enforcer (CI)
./mvnw -B -ntp verify                # unit + *IT (Docker)
./mvnw -B -ntp -Pcoverage verify     # coverage profile + Codecov upload (CI)
```

`<revision>` in `rose-build/pom.xml` is `0.0.1-SNAPSHOT`. CI and profile details: [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md).

## Packages

| Path | Purpose |
| --- | --- |
| `rose-bom/` | Consumer BOM — import in applications |
| `rose-foundation/` | Annotations, core utilities, test support |
| `rose-spring-boot/` | Bootstrap and baseline Boot starters |
| `rose-observation/` | Tracing, logs, OTLP, Micrometer bridges |
| `rose-multitenancy/` | Multitenancy support |
| `rose-mybatis-plus/` | MyBatis-Plus integration |
| `rose-devservice/` | Dev-service connectors (`rose-devservice-spring-boot-*`) |

Full reactor module list: root `pom.xml`.

## Documentation and help

| Topic | Link |
| --- | --- |
| Wiki hub | [wiki/Home.md](wiki/Home.md) |
| Getting started | [wiki/rose-spring-boot/Getting-Started.md](wiki/rose-spring-boot/Getting-Started.md) |
| Configuration | [wiki/rose-spring-boot/Configuration-Reference.md](wiki/rose-spring-boot/Configuration-Reference.md) |
| Consumer BOM | [wiki/rose-bom/Consumer-Guide.md](wiki/rose-bom/Consumer-Guide.md) |
| Build profiles & CI | [wiki/rose-build/Profiles-Management.md](wiki/rose-build/Profiles-Management.md) · [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md) · [Compatibility Matrix](wiki/rose-build/Compatibility-Matrix.md) |
| Design specs | [docs/design/README.md](docs/design/README.md) |
| Repository guide | [AGENTS.md](AGENTS.md) |
| CI improvements | [docs/ci-improvements.md](docs/ci-improvements.md) |
| Changelog | [CHANGELOG.md](CHANGELOG.md) |
| Issues | [GitHub Issues](https://github.com/zhijun-io/rose/issues) |

Sources under `wiki/` sync to the [GitHub Wiki](https://github.com/zhijun-io/rose/wiki) on push to `main` (`.github/workflows/wiki.yml`).

## Contributing

Maintained by [zhijun-io](https://github.com/zhijun-io). Implementation conventions, module layout, and PR expectations: [AGENTS.md](AGENTS.md).

## License

[Apache License 2.0](LICENSE)
