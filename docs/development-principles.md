# Rose development principles

**Status:** Normative guidance for contributors and reviewers.  
**Audience:** Anyone designing or reviewing changes in this repository.  
**Related:** [module-layering.md](module-layering.md) (structural rules), [CONTRIBUTING.md](../CONTRIBUTING.md) (workflow).

Rose is a **Spring Boot 2.7 / Java 8 extension platform**. These principles explain *why* the module layering and contribution rules exist. When a PR conflicts with a principle, fix the PR or document an explicit exception in the PR description.

---

## 1. Do not reinvent the wheel

**Prefer mature ecosystem libraries over custom implementations.**

| Need | Prefer | Avoid |
|------|--------|-------|
| Application runtime | Spring Boot 2.7, Spring Framework 5.x | Rebuilding Boot-style auto-configuration stacks |
| Observability | OpenTelemetry SDK, Micrometer, OTLP | Custom trace/metric protocols or exporters |
| Local dev dependencies | Testcontainers, official Docker images | Hand-rolled container lifecycle managers |
| HTTP / MVC | `spring-webmvc`, Servlet API | Parallel web abstractions |
| JDBC / data access | Spring Data JDBC, `spring-jdbc` | Custom ORM or connection pools in Rose |
| Version alignment | `spring-boot-dependencies`, `rose-bom` | Pinning duplicate or conflicting versions per module |

Add Rose code only where Boot or upstream libraries leave a **clear gap** (bootstrap, `rose.*` conventions, dev-services wiring, multitenancy hooks, etc.).

---

## 2. Extend Spring Boot; do not replace it

- Rose **extends** Spring Boot; it is **not** a fork or mirror of the Spring Boot project.
- Applications **should** keep `spring-boot-starter-parent` and **import** `rose-bom` for Rose artifact versions.
- Applications **must not** inherit `rose-parent` — that POM is for building Rose itself (reactor, `${revision}`, JaCoCo, release profile).
- Stay on **Java 8** and **Boot 2.7 / `javax.*`** until an explicit, project-wide migration is planned.

---

## 3. Compose before you duplicate

- **Reuse** `rose-core` and existing capability modules before introducing parallel utilities or wrappers.
- **Chain** starters (`rose-spring-boot-starter` → feature starters) instead of re-listing the same dependencies.
- **One capability, one module** — do not copy auto-configuration or properties classes across connectors; extract shared API (`rose-dev-services-api`, `rose-dev-services-core`) when behavior is common.
- New published libraries belong in **both** `rose-parent` and `rose-bom` `dependencyManagement` so consumers get a single version line.

---

## 4. Keep capabilities library-thin

Detailed rules live in [module-layering.md](module-layering.md). In short:

- Capability JARs use **fine-grained** Spring dependencies (`spring-webmvc`, `spring-boot-autoconfigure`, …).
- **`spring-boot-starter*`** belong in **starters** or application POMs, not in capability `compile` scope.
- Auto-configuration registers via **`META-INF/spring.factories`** (Boot 2.7).
- Rose-owned configuration uses the **`rose.*`** prefix only.

---

## 5. Minimal, correct changes

- Solve the stated problem; avoid drive-by refactors, unrelated formatting, or speculative abstractions.
- Match existing naming (`rose-*`, `io.zhijun.{domain}.*`) and code style in the module you touch.
- Add **integration tests** for dev-service connectors and other runtime wiring; unit tests should assert real behavior, not implementation trivia.
- Comments only for non-obvious business or integration rationale.

---

## 6. Conventions over one-off configuration

- **Bootstrap & profiles:** use Rose bootstrap APIs (`BootstrapMode`, dev/test profiles) instead of ad-hoc `EnvironmentPostProcessor` copies in every module.
- **Dev Services:** optional `runtime` connectors + matching Spring Boot starter for the technology; defaults (e.g. JDBC user/password/db `rose`) are overridable via `rose.dev.services.<connector>.*`.
- **Dependencies:** Renovate (`renovate.json`) enforces Boot 2.7 guardrails — do not bypass with manual major upgrades to Boot 3 / Jakarta without a migration plan.

---

## 7. Review gate (quick checklist)

Reviewers **should question** PRs that:

- Reimplement Spring Boot, OpenTelemetry, or Testcontainers features already available upstream
- Add `spring-boot-starter*` to capability `compile` dependencies without a documented exception
- Introduce a new module when extending an existing capability or starter would suffice
- Inherit or document `rose-parent` for external business applications
- Split `*-autoconfigure` prematurely or add `AutoConfiguration.imports` (Boot 3+) in this codebase
- Omit BOM entries, tests, or README updates for user-visible behavior

Valid exceptions **must** be explained in the PR and, if recurring, added as an amendment to [module-layering.md](module-layering.md) or this file.

---

## 8. Related documents

| Document | Role |
|----------|------|
| [module-layering.md](module-layering.md) | Normative module and dependency structure |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Build, test, release, PR workflow |
| [releasing.md](releasing.md) | Maven Central release workflow and checklist |
| [README.md](../README.md) | Consumer quick start and feature overview |
| `renovate.json` | Dependency update guardrails |
