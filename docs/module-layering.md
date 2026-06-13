# Rose module layering specification

**Status:** Normative — all new modules, starters, and dependency changes **must** follow this document.  
**Audience:** Contributors, reviewers, and automation.  
**Consumer overview:** See [README.md](../README.md#module-layout) for a shorter introduction.  
**Design values:** See [development-principles.md](development-principles.md) (reuse ecosystem libraries, extend Boot, minimal changes).

Rose is a **Spring Boot 2.7 / Java 8 extension platform**. It is **not** a mirror of the Spring Boot repository. Modules are organized so that **capabilities stay library-thin** and **starters own the runnable application stack**.

---

## 1. Layer model

```
Build & publish     rose-parent, rose-bom
        │
Base                rose-core → rose-spring-boot
        │
Capabilities        rose-{feature}/*  (libraries + embedded auto-configuration)
        │
Starters            rose-*-spring-boot-starter  (thin dependency aggregates)
```

| Layer | Artifact pattern | Purpose |
|-------|------------------|---------|
| **Build** | `rose-parent`, `rose-bom` | Reactor build, `${revision}`, third-party BOM imports, published version alignment |
| **Base** | `rose-core`, `rose-spring-boot` | Framework-agnostic utilities; Rose platform bootstrap (dev/test profiles, env post-processors) |
| **Capability** | `rose-{domain}/*`, `rose-dev-services-*` | Feature libraries; `@AutoConfiguration` lives **inside** the capability JAR |
| **Starter** | `rose-*-spring-boot-starter` | What applications depend on; aggregates capabilities + `spring-boot-starter*` |

**MUST NOT** add a separate `*-autoconfigure` module unless the capability has grown large enough to justify a split (not the default for Rose today).

---

## 2. Base layer

### `rose-core`

- **MUST** remain free of Spring Boot runtime requirements.
- **MAY** depend on `spring-core`, `slf4j-api`, and other framework-agnostic libraries.

### `rose-spring-boot`

- **MUST** depend on `rose-core`.
- **MUST** contain Rose-wide bootstrap integration only (not feature-specific logic).
- **MAY** depend on `spring-boot`, `spring-boot-autoconfigure`.

Capability modules **MUST NOT** depend on `rose-spring-boot` unless they integrate with Rose bootstrap APIs (`BootstrapMode`, `ConditionalOnDevMode`, etc.). The only current exception is **`rose-dev-services-core`**.

---

## 3. Capability layer

### 3.1 General rules

| Rule | Requirement |
|------|-------------|
| Rose base | **SHOULD** depend on `rose-core` as the primary `io.zhijun` dependency |
| Boot integration | **MAY** use `spring-boot`, `spring-boot-autoconfigure` (`<optional>true</optional>` when auto-config is optional at runtime) |
| Fine-grained Spring | **SHOULD** prefer `spring-webmvc`, `spring-data-jdbc`, `spring-jdbc`, `logback-classic`, `spring-boot-actuator-autoconfigure` over starters |
| Starters in capabilities | **MUST NOT** declare `spring-boot-starter*` at `compile` scope (except `spring-boot-starter-test` in `test` scope) |
| `rose-spring-boot` | **MUST NOT** depend on `rose-spring-boot` unless bootstrap integration is required |
| Feature stacking | **MAY** depend on other capability modules when the feature genuinely builds on them |
| Auto-configuration | **MUST** register via `META-INF/spring.factories` (Boot 2.7; not `AutoConfiguration.imports`) |
| Configuration prefix | **MUST** use `rose.*` for Rose-owned properties |

### 3.2 Approved dependency patterns

| Need | Capability module **SHOULD** use | Starter / application **SHOULD** use |
|------|----------------------------------|--------------------------------------|
| Web MVC | `spring-webmvc` + `javax.servlet-api` (`provided`) | `spring-boot-starter-web` |
| Micrometer metrics auto-config | `spring-boot-actuator-autoconfigure` | `spring-boot-starter-actuator` |
| JDBC dialect / converters | `spring-data-jdbc`, `spring-jdbc` | `spring-boot-starter-jdbc` or `spring-boot-starter-data-jdbc` |
| Logging bridge | `spring-boot`, `spring-boot-autoconfigure`, `logback-classic` | `spring-boot-starter` (logging) via baseline starter |
| Diagnostics (`FailureAnalyzer`) | `spring-boot` | — |

### 3.3 Reference capabilities (current codebase)

| Module | Rose deps | Spring integration |
|--------|-----------|------------------|
| `rose-multitenancy-core` | `rose-core` | optional `spring-boot-autoconfigure` |
| `rose-multitenancy-web` | `rose-core`, `rose-multitenancy-core` | `spring-webmvc` |
| `rose-opentelemetry-core` | `rose-core` | `spring-boot`, `spring-boot-autoconfigure` |
| `rose-opentelemetry-logback-bridge` | `rose-core`, `rose-opentelemetry-core` | `spring-boot`, `spring-boot-autoconfigure`, `logback-classic` |
| `rose-opentelemetry-micrometer-registry-otlp` | `rose-core`, `rose-opentelemetry-core` | `spring-boot-actuator-autoconfigure` |
| `rose-opentelemetry-micrometer-metrics-bridge` | `rose-core`, optional `rose-opentelemetry-core` | `spring-boot-actuator-autoconfigure`, `spring-boot-autoconfigure` |
| `rose-opentelemetry-semantic-conventions` | `rose-observation-core`, optional `rose-opentelemetry-core` | `spring-boot`, optional `spring-boot-autoconfigure` |
| `rose-observation-core` | `rose-core` | `spring-boot`, optional `spring-boot-autoconfigure` |
| `rose-data-jdbc-sqlite` | `rose-core` | `spring-data-jdbc`, `spring-jdbc` |
| `rose-dev-services-core` | `rose-dev-services-api`, **`rose-spring-boot`** | `spring-boot`, `spring-boot-autoconfigure` |
| `rose-dev-services-{connector}` | `rose-dev-services-core` | Testcontainers + technology-specific libs |

### 3.4 Anti-patterns (reject in review)

```xml
<!-- ❌ Capability module pulling a full starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- ❌ Capability module depending on platform starter -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot-starter</artifactId>
</dependency>

<!-- ❌ Unnecessary rose-spring-boot when only FailureAnalyzer / @Configuration is needed -->
<dependency>
    <groupId>io.zhijun</groupId>
    <artifactId>rose-spring-boot</artifactId>
</dependency>
```

```xml
<!-- ✅ Capability: fine-grained web -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
</dependency>

<!-- ✅ Starter: runnable web stack -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

## 4. Starter layer

### 4.1 General rules

| Rule | Requirement |
|------|-------------|
| Purpose | **MUST** be a thin POM: aggregate capability JARs + required `spring-boot-starter*` |
| Baseline | Feature starters **MUST** build on `rose-spring-boot-starter` (directly or transitively via another Rose starter) |
| No business logic | **MUST NOT** contain production Java sources (POM-only unless a rare exception is approved) |
| Naming | **MUST** use `rose-{feature}-spring-boot-starter` |
| Layering | **SHOULD** chain starters (e.g. web starter → core starter → baseline) instead of re-listing the baseline |

### 4.2 Current starter graph

```
rose-spring-boot-starter
├── rose-opentelemetry-spring-boot-starter
├── rose-multitenancy-core-spring-boot-starter
└── rose-multitenancy-web-spring-boot-starter
        └── (extends multitenancy-core-spring-boot-starter)
```

| Starter | Builds on | Adds (summary) |
|---------|-----------|----------------|
| `rose-spring-boot-starter` | — | `rose-spring-boot` + `spring-boot-starter` |
| `rose-opentelemetry-spring-boot-starter` | baseline | OTel modules + `spring-boot-starter-actuator` + OTLP runtime |
| `rose-multitenancy-core-spring-boot-starter` | baseline | `rose-multitenancy-core` |
| `rose-multitenancy-web-spring-boot-starter` | multitenancy-core starter | `rose-multitenancy-web` + `spring-boot-starter-web` |

### 4.3 Dev Services

- **MUST NOT** create per-connector starters.
- Connectors (`rose-dev-services-postgresql`, etc.) **MUST** remain optional `runtime` dependencies documented in README.

---

## 5. Build and BOM

| Rule | Requirement |
|------|-------------|
| Version | **MUST** use `${revision}` from `rose-parent` only |
| BOM cycle | `rose-parent` **MUST NOT** `import` `rose-bom` |
| Dual listing | New published artifacts **MUST** be added to **both** `rose-parent` and `rose-bom` `dependencyManagement` (keep lists identical) |
| Consumer usage | Applications **SHOULD** use `spring-boot-starter-parent` + `import rose-bom` + Rose starter(s) |

---

## 6. Adding a new module (checklist)

Use this checklist in every PR that adds or substantially changes a module.

### 6.1 Classify the module

- [ ] Identified layer: Base / Capability / Starter / Build-only
- [ ] Artifact name follows `rose-{domain}-{name}` convention
- [ ] Package under `io.zhijun.{domain}.*`

### 6.2 Capability module

- [ ] Depends on `rose-core` (not `rose-spring-boot-starter`)
- [ ] No `spring-boot-starter*` at compile scope
- [ ] `spring-boot-autoconfigure` marked `<optional>true</optional>` when appropriate
- [ ] `META-INF/spring.factories` registered if auto-configuring
- [ ] Properties use `rose.*` prefix
- [ ] If user-facing: companion starter added or README documents optional `runtime` dependency pattern

### 6.3 Starter module

- [ ] POM-only (no feature Java code)
- [ ] Extends `rose-spring-boot-starter` or another Rose starter
- [ ] Declares `spring-boot-starter*` that the capability module intentionally omitted

### 6.4 Build metadata

- [ ] `<module>` added to parent aggregator `pom.xml`
- [ ] Artifact added to `rose-parent` **and** `rose-bom` dependencyManagement
- [ ] README module table updated if user-visible
- [ ] `mvn validate` and `mvn test` pass (or `mvn verify -Pcoverage` before merge)

### 6.5 Review gate

Reviewers **MUST** reject PRs that:

- Add `spring-boot-starter*` to capability `compile` dependencies without documented exception
- Add `rose-spring-boot` to capabilities that do not use bootstrap APIs
- Publish a new user-facing library without BOM entry or consumption path (starter / README)
- Split `*-autoconfigure` prematurely

---

## 7. When to deviate

Deviations **MUST** be documented in the PR description and, if recurring, as an amendment to this file.

Valid reasons:

- Bootstrap integration (`rose-dev-services-core` → `rose-spring-boot`)
- Test-only dependencies (`spring-boot-starter-test`, `test` scope)
- Technology constraint that has no fine-grained Boot 2.7 artifact (rare; propose a doc update)

---

## 8. Related documents

| Document | Role |
|----------|------|
| [README.md](../README.md) | User-facing overview and examples |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Contribution workflow; links here as mandatory |
| [development-principles.md](development-principles.md) | Design values: reuse ecosystem, extend Boot, minimal changes |
| [releasing.md](releasing.md) | Maven Central release workflow and checklist |
| `renovate.json` | Dependency update guardrails (Spring Boot 2.7 / Java 8) |
| `pom.xml` (`rose-parent`) | Authoritative reactor module list |
| `rose-bom/pom.xml` | Authoritative published artifact list |
