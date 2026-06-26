# Compatibility Matrix

Supported runtime, build, and third-party versions for **Rose** (`io.zhijun`) and for **applications that import `rose-bom`**.

Sources of truth:

| Source | Location |
|--------|----------|
| Spring Boot pin | `rose-build/pom.xml` → `spring-boot.version` |
| Testcontainers pin | `rose-parent/pom.xml` → `testcontainers.version` |
| Dependency upgrades policy | [`renovate.json`](../../renovate.json) |
| CI verification | [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) |

---

## Summary

| Dimension | Rose baseline | Consumer guidance |
|-----------|---------------|-------------------|
| **Spring Boot** | **2.7.18** | Stay on **2.7.x**; do not mix Boot 3+ with current Rose releases |
| **Java (bytecode)** | **8** (compile target) | Run on **Java 8+**; Rose JARs are Java 8–compatible |
| **Java EE / Jakarta** | **`javax.*`** (via Boot 2.7) | Do not force Jakarta-only stacks onto Rose 2.7 artifacts |
| **Testcontainers** (dev services) | **1.21.4** (max on Boot 2.7) | Pin ≤ **1.21.4** if you manage Testcontainers yourself |
| **Maven** | **≥ 3.6** (enforcer) | Use `./mvnw` from the repo (Maven 3.9.x wrapper) |
| **Docker** | Required for `*IT` | Required when using `rose-devservice-spring-boot-*` at dev/test time |

---

## Java

### Compile vs run

| Role | Version | Notes |
|------|---------|-------|
| **Rose reactor compile target** | **8** | `rose-build/pom.xml` → `java.version` |
| **Local dev (recommended)** | **8** | `.sdkmanrc` → `java=8.0.x-tem` |
| **Consumer minimum** | **8** | Rose artifacts target Java 8 bytecode |
| **Consumer tested (CI)** | **8, 11, 17, 21, 25** | Unit tests (`ci.yml` matrix) |
| **Integration tests (CI)** | **17** | Failsafe `*IT`, JaCoCo, Codecov |
| **Maven Central publish** | **8** | `publish.yml` |

Rose does **not** require consumers to run Java 17 unless you rely on features that need a newer JDK. CI proves Rose builds and unit-tests across multiple LTS and current JDKs while keeping **Java 8 bytecode**.

### Not supported (current `main` line)

| Item | Reason |
|------|--------|
| **Java 7 or below** | Enforcer `requireJavaVersion` `[1.8,)` |
| **Recompiling Rose on Java 8 with `--release` removed** | Would break consumer Java 8 promise |

---

## Spring Boot & Spring Framework

| Component | Version | Managed by |
|-----------|---------|------------|
| **Spring Boot** | **2.7.18** | `spring-boot-dependencies` BOM import in `rose-parent` |
| **Spring Framework** | **5.3.x** | Transitive from Boot 2.7 |
| **Jackson** | **2.13.x** | Transitive from Boot 2.7 |
| **Servlet API** | **`javax.servlet`** | Boot 2.7 / Tomcat 9.x |
| **Spring Boot 3.x / 4.x** | ❌ Not supported | Renovate `allowedVersions: <3.0.0` for `org.springframework.boot:**` |
| **Spring Framework 6+** | ❌ Not supported | Renovate blocks `<6.0.0` for `org.springframework:**` |
| **Jakarta EE (`jakarta.*`)** | ❌ Not on this line | Renovate disables Jakarta artifact upgrades |

### Consumer parent POM

Applications should use **`spring-boot-starter-parent`** (2.7.x) or a corporate BOM aligned with Boot 2.7, then **import `rose-bom`** — not `rose-parent` or `rose-build`.

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
            <version><!-- released version --></version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## Testcontainers & Dev Services

Rose **dev services** (`rose-devservice-spring-boot-*`) depend on Testcontainers at **test/dev** time.

| Item | Value |
|------|-------|
| **Pinned BOM version** | **1.21.4** (`rose-parent` → `testcontainers.version`) |
| **Renovate policy** | All `org.testcontainers:**` upgrades **disabled** |
| **Docker** | Required on the machine running `*IT` or dev-services integration |
| **Scope in apps** | Connectors are typically `runtime` + `optional`; disable in prod with `rose.dev.enabled=false` |

### Why Testcontainers is capped at 1.21.4

Testcontainers **2.x** bundles a shaded Jackson that expects **≥ 2.15**. Spring Boot **2.7** ships Jackson **2.13.x**. Mixing Testcontainers 2.x with Boot 2.7 causes runtime failures such as:

```text
java.lang.NoSuchFieldError: READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
```

Reference: [testcontainers-java#11236](https://github.com/testcontainers/testcontainers-java/issues/11236)

**Implications:**

| Scenario | Guidance |
|----------|----------|
| Use Rose dev-service connectors only | Rely on `rose-bom`; no extra Testcontainers pin needed |
| Your app also declares Testcontainers | Align to **1.21.4** (or stay on 1.x compatible with Jackson 2.13) |
| Upgrade to Testcontainers 2.x | Requires **Spring Boot 3+** and a **future Rose release line** (not `main` today) |

### Maven artifact names (1.x vs 2.x)

Rose uses **Testcontainers 1.x** coordinates, for example:

| 1.x (Rose) | 2.x (do not use on Boot 2.7) |
|------------|------------------------------|
| `org.testcontainers:jdbc` | `org.testcontainers:testcontainers-jdbc` |
| `org.testcontainers:testcontainers` | (module layout changed) |
| `org.testcontainers:postgresql` | `org.testcontainers:testcontainers-postgresql` |

---

## Rose-managed third-party pins

These are declared in `rose-parent/pom.xml` and exposed through `rose-bom` where applicable:

| Library | Version | Used by |
|---------|---------|---------|
| **MyBatis-Plus** | **3.5.16** | `rose-mybatis-plus` |
| **OpenTelemetry SDK** | **1.63.0** | `rose-observation` |
| **OpenTelemetry Instrumentation** | **2.29.0** | `rose-observation` |
| **OpenTelemetry semconv** | **1.42.0** | `rose-observation` |
| **Kotlin stdlib** (optional/transitive) | **2.2.21** | OTel OTLP HTTP client chain |

Patch upgrades may arrive via Renovate on a schedule; see [`renovate.json`](../../renovate.json) labels (`spring-boot-2`).

---

## CI verification map

What GitHub Actions proves on each push/PR to `main`:

| Job | JDK | Command | Scope |
|-----|-----|---------|-------|
| **Unit tests** | 8, 11, 17, 21, 25 | `./mvnw -B -ntp validate` then `./mvnw -B -ntp verify -DskipITs` | Enforcer + Surefire `*Test` / `*Tests` |
| **Integration tests** | 17 | `./mvnw -B -ntp -Pcoverage verify` | Failsafe `*IT`, Testcontainers, JaCoCo, Codecov |

Details: [CI/CD Integration](CI-CD-Integration).

---

## Support policy (informal)

| Line | Branch | Boot | Java bytecode | Status |
|------|--------|------|---------------|--------|
| **Current** | `main` | 2.7.x | 8 | Active development |
| **Boot 3 / Jakarta** | — | 3.x+ | 17+ | **Not started**; see [roadmap](../../docs/design/rose-next-iteration-roadmap.md) Phase 3 |

There is no separate LTS branch yet. Released versions follow tags (`v*`) and [CHANGELOG](../../CHANGELOG.md).

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `NoSuchFieldError` … `READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE` | Testcontainers **2.x** on Boot **2.7** | Pin Testcontainers **1.21.4** or use `rose-bom` only |
| `*IT` skipped locally but fails in CI | Docker not running | Start Docker; run `./mvnw verify` without `-DskipITs` |
| `javax` / `jakarta` clash | Boot 3 dependency on classpath | Stay on Boot **2.7.x** with current Rose |
| Enforcer fails on Maven version | Maven &lt; 3.6 | Use `./mvnw` |
| Unit tests pass on JDK 21 but you deploy on 8 | Rare API misuse in app code | Rose itself is verified on JDK 8 in CI |

---

## Related documentation

| Topic | Link |
|-------|------|
| BOM import & starters | [Consumer Guide](../rose-bom/Consumer-Guide) |
| Build profiles | [Profiles Management](Profiles-Management) |
| CI workflows | [CI/CD Integration](CI-CD-Integration) |
| Iteration roadmap | [rose-next-iteration-roadmap.md](../../docs/design/rose-next-iteration-roadmap.md) |
| Contributing | [docs/rose-conventions.md](../../docs/rose-conventions.md) |
