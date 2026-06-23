# Getting Started

This guide covers two audiences:

1. **Application developers** — consume Rose via `rose-bom` and starters.
2. **Rose contributors** — build the reactor and validate `rose-build` profiles.

---

## Prerequisites

| Requirement | Minimum | Notes |
|---|---|---|
| **JDK** | 8 | CI tests 8, 11, 17, 21, 25 |
| **Maven** | 3.6+ | Enforced by `rose-build` enforcer |
| **Docker** | — | Required for `*IT` integration tests and dev-service connectors |

---

## Part 1 — Use Rose in Your Application

Rose extends Spring Boot; it does **not** replace `spring-boot-starter-parent`.

### Step 1 — Import the BOM

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

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
```

Use a released version from [Maven Central](https://central.sonatype.com/) when available.

### Step 2 — Add a Starter

```xml
<dependencies>
    <dependency>
        <groupId>io.zhijun</groupId>
        <artifactId>rose-spring-boot-core</artifactId>
    </dependency>
</dependencies>
```

Feature starters (OpenTelemetry, multitenancy, MyBatis-Plus) transitively include the baseline starter — declare them **instead of** duplicating `rose-spring-boot-core`. See [Consumer Guide](Consumer-Guide).

### Step 3 — Optional Dev Services

Add a connector with `runtime` + `optional` scope and the matching Spring Boot starter (e.g. JDBC):

```xml
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-devservice-spring-boot-postgresql</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

- Global toggle: `rose.dev.enabled` (default `true`)
- Per-connector: `rose.dev.<connector>.enabled` (e.g. `rose.dev.postgresql.enabled`)

Details: [rose-devservice/README.md](../rose-devservice/README.md).

---

## Part 2 — Build This Repository

```bash
git clone https://github.com/zhijun-io/rose.git
cd rose

mvn validate          # enforcer rules
mvn test              # unit test (*Test / *Tests)
mvn verify            # unit + integration (*IT; needs Docker)
mvn verify -DskipITs  # skip integration test
mvn verify -Pcoverage # JaCoCo reports (CI uses JDK 21)
```

Version is defined once in `rose-build/pom.xml` → `<revision>`. Override at build time:

```bash
mvn test -Drevision=1.0.0-SNAPSHOT
```

---

## Next Steps

- [Consumer Guide](Consumer-Guide) — starter matrix and configuration prefixes
- [Profiles Management](Profiles-Management) — Maven profile reference
- [Configuration Reference](Configuration-Reference) — module map and property keys
