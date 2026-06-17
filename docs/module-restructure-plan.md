# Rose module restructure plan

**Status:** Implemented.  
**Audience:** Maintainers and contributors performing the module layout migration.  
**Related:** [README.md](../README.md#module-layout), [module-layering.md](module-layering.md), [development-principles.md](development-principles.md).

This document defines the target module layout for Rose after the repository structure cleanup discussed during architecture review. The goal is to make the repository easier to navigate by grouping only the themes that truly behave like module families, while keeping single-purpose modules flat at the repository root.

The plan follows these principles:

- Keep build and platform foundation modules at the repository root.
- Keep theme directories only when a theme contains multiple strongly related submodules.
- Do **not** introduce a `rose-data/` grouping layer.
- Do **not** keep a central `rose-spring-boot-starters/` directory long term.
- Prefer minimal structural change first; avoid unnecessary artifact renames when there is no clear benefit.

---

## 1. Target layout

```text
rose/
├── pom.xml
├── rose-bom
├── rose-core
├── rose-spring-boot
├── rose-spring-boot-starter
├── rose-excel
├── rose-sqlite
├── rose-mybatis-plus
│   ├── rose-mybatis-plus-core
│   ├── rose-mybatis-plus-spring-boot-starter
│   └── README.md
├── rose-observation
│   ├── rose-observation-core
│   └── README.md
├── rose-opentelemetry
│   ├── rose-opentelemetry-core
│   ├── rose-opentelemetry-logback-bridge
│   ├── rose-opentelemetry-micrometer-metrics-bridge
│   ├── rose-opentelemetry-micrometer-registry-otlp
│   ├── rose-opentelemetry-semantic-conventions
│   ├── rose-opentelemetry-spring-boot-starter
│   └── README.md
├── rose-multitenancy
│   ├── rose-multitenancy-core
│   ├── rose-multitenancy-web
│   ├── rose-multitenancy-core-spring-boot-starter
│   ├── rose-multitenancy-web-spring-boot-starter
│   └── README.md
├── rose-dev-services
│   ├── rose-dev-services-api
│   ├── rose-dev-services-core
│   ├── rose-dev-services-activemq
│   ├── rose-dev-services-artemis
│   ├── rose-dev-services-kafka
│   ├── rose-dev-services-mongodb
│   ├── rose-dev-services-mqtt
│   ├── rose-dev-services-mysql
│   ├── rose-dev-services-ollama
│   ├── rose-dev-services-openlit
│   ├── rose-dev-services-otel-collector
│   ├── rose-dev-services-postgresql
│   ├── rose-dev-services-rabbitmq
│   ├── rose-dev-services-redis
│   ├── rose-dev-services-tests
│   └── README.md
└── docs
```

---

## 2. Design decisions

### 2.1 Root-level modules

The following modules stay flat at the repository root because they are either foundational or single-purpose enough that an extra directory adds more ceremony than value:

- `rose-bom`
- `rose-core`
- `rose-spring-boot`
- `rose-spring-boot-starter`
- `rose-excel`
- `rose-sqlite`

### 2.2 Theme directories

The following themes remain grouped because they already behave like true module families:

- `rose-mybatis-plus`
- `rose-opentelemetry`
- `rose-multitenancy`
- `rose-dev-services`

`rose-observation` may be flattened in the future if it remains a single-submodule theme, but it is acceptable to keep it grouped for now to minimize churn.

### 2.3 Naming rules

- Single-purpose capability module: `rose-{capability}`
- Technology-specific single module: `rose-{technology}`
- Theme directory: `rose-{theme}`
- Theme core module: `rose-{theme}-core`
- Starter: `rose-{capability-or-theme}-spring-boot-starter`

Examples:

- `rose-data-excel` -> `rose-excel`
- `rose-data-jdbc-sqlite` -> `rose-sqlite`
- `rose-data-mybatis-plus` -> `rose-mybatis-plus/rose-mybatis-plus-core`

---

## 3. Module mapping

### 3.1 Modules kept as-is

| Current path | Target path | Artifact action |
|--------------|-------------|-----------------|
| `rose-bom` | `rose-bom` | keep |
| `rose-core` | `rose-core` | keep |
| `rose-spring-boot` | `rose-spring-boot` | keep |
| `rose-observation` | `rose-observation` | keep for now |
| `rose-opentelemetry` | `rose-opentelemetry` | keep |
| `rose-multitenancy` | `rose-multitenancy` | keep |
| `rose-dev-services` | `rose-dev-services` | keep |

### 3.2 Root-level rename and move

| Current path | Target path | Current artifactId | Target artifactId |
|--------------|-------------|--------------------|-------------------|
| `rose-data/rose-data-excel` | `rose-excel` | `rose-data-excel` | `rose-excel` |
| `rose-data/rose-data-jdbc-sqlite` | `rose-sqlite` | `rose-data-jdbc-sqlite` | `rose-sqlite` |
| `rose-spring-boot-starters/rose-spring-boot-starter` | `rose-spring-boot-starter` | `rose-spring-boot-starter` | keep |

### 3.3 MyBatis Plus theme extraction

| Current path | Target path | Current artifactId | Target artifactId |
|--------------|-------------|--------------------|-------------------|
| `rose-data/rose-data-mybatis-plus` | `rose-mybatis-plus/rose-mybatis-plus-core` | `rose-data-mybatis-plus` | `rose-mybatis-plus-core` |
| `rose-spring-boot-starters/rose-data-mybatis-plus-spring-boot-starter` | `rose-mybatis-plus/rose-mybatis-plus-spring-boot-starter` | `rose-data-mybatis-plus-spring-boot-starter` | `rose-mybatis-plus-spring-boot-starter` |

### 3.4 Starter relocation by theme

| Current path | Target path | Current artifactId | Target artifactId |
|--------------|-------------|--------------------|-------------------|
| `rose-spring-boot-starters/rose-opentelemetry-spring-boot-starter` | `rose-opentelemetry/rose-opentelemetry-spring-boot-starter` | `rose-opentelemetry-spring-boot-starter` | keep |
| `rose-spring-boot-starters/rose-multitenancy-core-spring-boot-starter` | `rose-multitenancy/rose-multitenancy-core-spring-boot-starter` | `rose-multitenancy-core-spring-boot-starter` | keep |
| `rose-spring-boot-starters/rose-multitenancy-web-spring-boot-starter` | `rose-multitenancy/rose-multitenancy-web-spring-boot-starter` | `rose-multitenancy-web-spring-boot-starter` | keep |

### 3.5 Directories removed after migration

- `rose-data`
- `rose-spring-boot-starters`

These directories should only be removed after all child modules have been moved and the reactor build passes.

---

## 4. Implementation sequence

### Phase 1: Move directories without changing behavior

1. Move starter modules out of `rose-spring-boot-starters` into their target locations.
2. Move `rose-data/rose-data-excel` to `rose-excel`.
3. Move `rose-data/rose-data-jdbc-sqlite` to `rose-sqlite`.
4. Move `rose-data/rose-data-mybatis-plus` into `rose-mybatis-plus/`.
5. Update root `pom.xml` `<modules>` order and paths.
6. Update parent POM references and `relativePath` values inside moved modules.

At the end of this phase, it is acceptable to keep old artifactIds temporarily if that reduces risk during the directory migration.

### Phase 2: Rename artifacts

1. Rename `rose-data-excel` to `rose-excel`.
2. Rename `rose-data-jdbc-sqlite` to `rose-sqlite`.
3. Rename `rose-data-mybatis-plus` to `rose-mybatis-plus-core`.
4. Rename `rose-data-mybatis-plus-spring-boot-starter` to `rose-mybatis-plus-spring-boot-starter`.
5. Update references in:
   - root `pom.xml`
   - `rose-bom/pom.xml`
   - dependencyManagement in the root parent
   - module POM dependencies
   - `README.md`
   - release notes or changelog entries if the rename is user-visible

### Phase 3: Documentation alignment

1. Update root `README.md` module layout examples.
2. Update `docs/module-layering.md` naming examples.
3. Add or refresh theme-local `README.md` files:
   - `rose-mybatis-plus/README.md`
   - `rose-opentelemetry/README.md`
   - `rose-multitenancy/README.md`
   - `rose-dev-services/README.md`

---

## 5. Detailed checklist

### 5.1 Reactor and parent POMs

- [ ] Update root `pom.xml` `<modules>`
- [ ] Update nested aggregator `pom.xml` files if child paths changed
- [ ] Update `<artifactId>` where renames are planned
- [ ] Update `<parent><relativePath>` in moved modules

### 5.2 Dependency management

- [ ] Update root parent `dependencyManagement`
- [ ] Update `rose-bom/pom.xml`
- [ ] Update inter-module dependencies
- [ ] Update any integration-test module dependencies

### 5.3 Metadata and docs

- [ ] Update root `README.md`
- [ ] Update `docs/module-layering.md`
- [ ] Update `CONTRIBUTING.md` if module paths are mentioned
- [ ] Add theme README files if missing

### 5.4 Verification

- [ ] `mvn validate`
- [ ] `mvn test -pl rose-excel`
- [ ] `mvn test -pl rose-sqlite`
- [ ] `mvn test -pl rose-mybatis-plus/rose-mybatis-plus-core`
- [ ] `mvn test -pl rose-opentelemetry/...` for any moved starter dependencies if needed
- [ ] `mvn test -pl rose-multitenancy/...` for moved starter dependencies if needed
- [ ] `mvn test`

If CI coverage is expected before merge, also run:

- [ ] `mvn verify -Pcoverage`

---

## 6. Compatibility and risk notes

### 6.1 Maven coordinates

Artifact renames are user-visible and should be treated as a breaking change for consumers. If external users already depend on:

- `rose-data-excel`
- `rose-data-jdbc-sqlite`
- `rose-data-mybatis-plus`
- `rose-data-mybatis-plus-spring-boot-starter`

then the release notes must clearly document the new coordinates.

If backward compatibility is required for one release window, consider temporary relocation POMs or explicit migration notes.

### 6.2 Scope control

This restructuring should remain structural. Avoid mixing it with:

- behavior changes
- dependency upgrades
- package renames unless strictly necessary
- API redesign

### 6.3 Observation theme

`rose-observation` can remain grouped for now even though it currently has a single child module. Keeping it unchanged reduces churn and avoids turning this migration into a repository-wide flattening exercise.

---

## 7. Success criteria

The migration is complete when:

- No production module remains under `rose-data/`
- No starter remains under `rose-spring-boot-starters/`
- Root and nested reactor builds pass
- BOM and parent dependency management reflect the new artifact names
- README and docs match the new structure
- Consumers can understand the repository by scanning the root and theme directories alone
