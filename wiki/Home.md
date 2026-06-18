# Rose

**Rose** is a Spring Boot 2.7 / Java 8 extension platform (`io.zhijun`): optional starters and libraries for bootstrap utilities, OpenTelemetry, multitenancy, dev services, and related capabilities.

| | |
|---|---|
| **Group ID** | `io.zhijun` |
| **BOM** | `rose-bom` |
| **License** | Apache License 2.0 |
| **Java (tested in CI)** | 8, 11, 17, 21, 25 |
| **Spring Boot** | 2.7.x |

---

## Wiki Pages

| Page | Description |
|---|---|
| [Home](Home) | This page |

> Extended guides live in the main repository under `docs/` and module README files. Add new wiki pages as `wiki/*.md`; they sync to GitHub Wiki on push to `main`.

---

## Quick Start

Import the Rose BOM and add a starter:

```xml
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

See the [repository README](https://github.com/zhijun-io/rose/blob/main/README.md) for full getting started instructions.

---

## Links

- **Source Code:** https://github.com/zhijun-io/rose
- **Issue Tracker:** https://github.com/zhijun-io/rose/issues
- **CI:** https://github.com/zhijun-io/rose/actions/workflows/maven-build.yml
