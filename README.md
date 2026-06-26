# Rose

[![CI](https://github.com/zhijun-io/rose/actions/workflows/ci.yml/badge.svg)](https://github.com/zhijun-io/rose/actions/workflows/ci.yml) [![License](https://img.shields.io/github/license/zhijun-io/rose)](LICENSE) [![codecov](https://codecov.io/gh/zhijun-io/rose/graph/badge.svg)](https://codecov.io/gh/zhijun-io/rose) ![Java 8+](https://img.shields.io/badge/Java-8+-orange?logo=openjdk&logoColor=white) ![Spring Boot 2.7.18](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white)

Spring Boot **2.7** / Java **8** extension libraries and starters for **`io.zhijun`** — bootstrap utilities, OpenTelemetry, multitenancy, MyBatis-Plus, and Testcontainers-based dev services.

Applications import **`rose-bom`** for version alignment. Do **not** use `rose-build` or `rose-parent` as your application parent.

## Why use Rose

- **BOM-aligned versions** — one `rose-bom` import syncs all `io.zhijun` artifacts
- **Composable starters** — observation, multitenancy, MyBatis-Plus, baseline bootstrap
- **Dev services** — optional Testcontainers connectors with dynamic properties (`rose-devservice-spring-boot-*`)
- **OpenTelemetry-first** — tracing, logs, OTLP, Micrometer bridges
- **Build governance** — shared `rose-build` enforcer, profiles, JaCoCo (this reactor only)

## Getting started

### In your application

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
        <artifactId>rose-spring-boot-core</artifactId>
    </dependency>
</dependencies>
```

Use a released coordinate from [Maven Central](https://central.sonatype.com/) when published. Feature starters supersede duplicating `rose-spring-boot-core` — see [wiki/rose-bom/Consumer-Guide.md](wiki/rose-bom/Consumer-Guide.md).

Step-by-step: [wiki/rose-spring-boot/Getting-Started.md](wiki/rose-spring-boot/Getting-Started.md).

### Build this repository

```bash
git clone https://github.com/zhijun-io/rose.git
cd rose
sdk env                         # optional — Java 8 (.sdkmanrc)
./mvnw -B validate              # enforcer (CI)
./mvnw -B verify                # unit + *IT (Docker)
./mvnw -B -Pcoverage verify     # CI coverage + Codecov upload
```

`<revision>` in `rose-build/pom.xml` is `0.0.1-SNAPSHOT`. Parity details: [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md).

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

Full reactor: root `pom.xml`.

## Documentation

| Topic | Link |
| --- | --- |
| Overview | [wiki/Home.md](wiki/Home.md) |
| Getting started | [wiki/rose-spring-boot/Getting-Started.md](wiki/rose-spring-boot/Getting-Started.md) |
| Configuration | [wiki/rose-spring-boot/Configuration-Reference.md](wiki/rose-spring-boot/Configuration-Reference.md) |
| Consumer BOM | [wiki/rose-bom/Consumer-Guide.md](wiki/rose-bom/Consumer-Guide.md) |
| Build & CI | [wiki/rose-build/Profiles-Management.md](wiki/rose-build/Profiles-Management.md) · [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md) |
| Design specs | [docs/design/README.md](docs/design/README.md) |
| Conventions (中文) | [docs/rose-conventions.md](docs/rose-conventions.md) |

`wiki/` sources sync to [GitHub Wiki](https://github.com/zhijun-io/rose/wiki) on push to `main` (`.github/workflows/wiki.yml`).

## Contributing

See [docs/rose-conventions.md](docs/rose-conventions.md). Issues: [GitHub Issues](https://github.com/zhijun-io/rose/issues).

## License

[Apache License 2.0](LICENSE)
