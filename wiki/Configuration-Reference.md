# Configuration Reference

Key properties, module layout, and customization points for the Rose repository.

---

## Version (`${revision}`)

| Item | Location |
|---|---|
| Source of truth | `rose-build/pom.xml` → `<revision>` |
| Current value | `0.0.1-SNAPSHOT` |
| Override at build | `mvn test -Drevision=1.0.0-SNAPSHOT` |
| Flattening | `flatten-maven-plugin` (`resolveCiFriendliesOnly`) |

Consumer applications pin **`rose-bom`** version in `dependencyManagement`; they do not use `${revision}`.

---

## Three-Layer Maven Layout

| Layer | Artifact | Path | Consumer inherits? |
|---|---|---|---|
| Build | `rose-build` | `rose-build/` | No |
| Parent | `rose-parent` | root `pom.xml` | No (reactor only) |
| BOM | `rose-bom` | `rose-bom/` | Yes (import) |

---

## Reactor Modules

| Directory | Description |
|---|---|
| `rose-build` | Shared build parent POM |
| `rose-bom` | Bill of Materials for consumers |
| `rose-core` | Core utilities |
| `rose-spring` | Spring Framework extensions |
| `rose-spring-boot` | Spring Boot utilities and starters |
| `rose-mybatis-plus` | MyBatis-Plus extensions |
| `rose-observation` | Observation / OpenTelemetry integration |
| `rose-multitenancy` | Multitenancy core, spring, starter |
| `rose-devservice` | Testcontainers dev services |

---

## Locked Dependency Versions (reactor)

Defined in root `rose-parent` `properties`:

| Property | Purpose |
|---|---|
| `spring-boot.version` | Spring Boot BOM (2.7.18) |
| `testcontainers.version` | Testcontainers BOM |
| `micrometer.version` | Micrometer BOM |
| `opentelemetry.version` | OpenTelemetry SDK BOM |
| `mybatis-plus.version` | MyBatis-Plus |

Third-party versions are **not** re-exported through `rose-bom`; consumers rely on `spring-boot-starter-parent` for non-`io.zhijun` coordinates.

---

## Resource Filtering

Inherited from `rose-build`:

```xml
<resources>
    <resource>
        <directory>src/main/java</directory>
        <filtering>true</filtering>
        <includes>
            <include>**/*.properties</include>
            <include>**/*.xml</include>
        </includes>
    </resource>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
    </resource>
</resources>
```

Delimiter: `@` (`resource.delimiter` property).

---

## Enforcer Rules (`validate` phase)

| Rule | Constraint |
|---|---|
| `requireMavenVersion` | Maven ≥ 3.6 |
| `requireJavaVersion` | Build JDK ≥ 8 |
| `banDuplicatePomDependencyVersions` | No duplicate dependency versions in a POM |
| `bannedDependencies` | Placeholder for future restrictions |

---

## Application Configuration (Rose runtime)

See [Consumer Guide](Consumer-Guide) for starter-specific prefixes:

- `rose.otel.*` — OpenTelemetry
- `rose.multitenancy.*` — Multitenancy
- `rose.dev.enabled` — Global dev services toggle
- `rose.dev.<connector>.*` — Per-connector dev service settings

Default Boot auto-configuration exclusions ship in `META-INF/config/default/*.properties` (see `rose-spring-boot-core`).

---

## Design Documents

| Document | Topic |
|---|---|
| [rose-spring-property-source-design.md](../docs/rose-spring-property-source-design.md) | Property sources |
| [rose-spring-env-refresh-design.md](../docs/rose-spring-env-refresh-design.md) | Environment refresh |
| [rose-spring-boot-bootstrap-diagnostics-design.md](../docs/rose-spring-boot-bootstrap-diagnostics-design.md) | Bootstrap diagnostics |
