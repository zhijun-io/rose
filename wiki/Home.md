# Rose

**Rose** is a Spring Boot 2.7 / Java 8 extension platform (`io.zhijun`): optional starters and libraries for bootstrap utilities, OpenTelemetry, multitenancy, dev services, and related capabilities.

| | |
|---|---|
| **Group ID** | `io.zhijun` |
| **BOM** | `rose-bom` |
| **Build parent** | `rose-build` |
| **License** | Apache License 2.0 |
| **Java (tested in CI)** | 8, 11, 17, 21, 25 |
| **Spring Boot** | 2.7.x |

---

## Key Features

| Feature | Description |
|---|---|
| **BOM-aligned versions** | Import `rose-bom` once; all `io.zhijun` artifacts stay in sync |
| **Composable starters** | Observability, multitenancy, MyBatis-Plus, baseline bootstrap |
| **Dev services** | Testcontainers connectors with dynamic properties (`rose-devservice-spring-boot-*`) |
| **OpenTelemetry-first** | Tracing, logs, OTLP metrics, optional Micrometer bridges |
| **Build governance** | Shared `rose-build` parent (profiles, JaCoCo, Central publishing) |

---

## Wiki Pages

| Page | Description |
|---|---|
| [Home](Home) | This page |
| [Getting Started](Getting-Started) | Prerequisites, BOM import, local build |
| [Consumer Guide](Consumer-Guide) | Application POM patterns and starter selection |
| [Profiles Management](Profiles-Management) | Maven profiles (`coverage`, `release`, JDK-activated) |
| [CI/CD Integration](CI-CD-Integration) | GitHub Actions workflows |
| [Configuration Reference](Configuration-Reference) | `${revision}`, properties, module map |

> Wiki sources live in `wiki/*.md` and sync to GitHub Wiki on push to `main` (see `.github/workflows/publish-wiki.yml`).

Design notes and ADR-style documents: [`docs/`](../docs/) in the main repository.

---

## Quick Start

Import the Rose BOM and add a starter:

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

See [Getting Started](Getting-Started) and the [repository README](https://github.com/zhijun-io/rose/blob/main/README.md).

---

## Links

- **Source Code:** https://github.com/zhijun-io/rose
- **Issue Tracker:** https://github.com/zhijun-io/rose/issues
- **CI:** https://github.com/zhijun-io/rose/actions/workflows/maven-build.yml
- **Reference build:** https://github.com/microsphere-projects/microsphere-build
