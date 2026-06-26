# Rose

**Rose** is a Spring Boot 2.7 / Java 8 extension platform (`io.zhijun`): optional starters and libraries for bootstrap utilities, OpenTelemetry, multitenancy, dev services, and related capabilities.

| | |
|---|---|
| **Group ID** | `io.zhijun` |
| **BOM** | `rose-bom` |
| **Build parent** | `rose-build` |
| **License** | Apache License 2.0 |
| **Java (tested in CI)** | 8, 11, 17, 21 (unit); 25 (coverage, IT, CodeQL) |
| **Spring Boot** | 2.7.x |

---

## Key Features

| Feature | Description |
|---|---|
| **BOM-aligned versions** | Import `rose-bom` once; all `io.zhijun` artifacts stay in sync |
| **Composable starters** | Observation, multitenancy, MyBatis-Plus, baseline bootstrap |
| **Dev services** | Testcontainers connectors with dynamic properties (`rose-devservice-spring-boot-*`) |
| **OpenTelemetry-first** | Tracing, logs, OTLP metrics, optional Micrometer bridges |
| **Build governance** | Shared `rose-build` parent (profiles, JaCoCo, Central publishing) |

---

## Wiki by module

| Module | Pages |
|--------|-------|
| [rose-spring-boot](rose-spring-boot/Getting-Started) | [Getting Started](rose-spring-boot/Getting-Started), [Configuration Reference](rose-spring-boot/Configuration-Reference) |
| [rose-bom](rose-bom/Consumer-Guide) | [Consumer Guide](rose-bom/Consumer-Guide) |
| [rose-build](rose-build/Profiles-Management) | [Profiles Management](rose-build/Profiles-Management), [CI/CD Integration](rose-build/CI-CD-Integration) |

> Sources under `wiki/<module>/`. Synced to GitHub Wiki on push to `main` (`.github/workflows/publish-wiki.yml`).

Design specs: [`docs/design/`](../docs/design/). Contributing: [`docs/rose-conventions.md`](../docs/rose-conventions.md).

---

## Quick Start

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

See [Getting Started](rose-spring-boot/Getting-Started) and the [repository README](https://github.com/zhijun-io/rose/blob/main/README.md).

---

## Links

- **Source Code:** https://github.com/zhijun-io/rose
- **Issue Tracker:** https://github.com/zhijun-io/rose/issues
- **CI:** https://github.com/zhijun-io/rose/actions/workflows/maven-build.yml
- **Reference build:** https://github.com/microsphere-projects/microsphere-build
