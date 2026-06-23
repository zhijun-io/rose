# Microsphere benchmark notes for Rose

**Status:** Reference notes for architecture and roadmap discussion.  
**Audience:** Maintainers planning future Rose capabilities, module boundaries, and documentation.  
**Related:** 根 [README.md](../README.md), [microsphere-benchmark-notes.md](microsphere-benchmark-notes.md)（本文件）.

This document summarizes what Rose can learn from the public Microsphere project family. The intent is **not** to copy Microsphere wholesale. The goal is to identify practices that match Rose's current position as a **Spring Boot 2.7 / Java 8 extension platform**.

The analysis is based on public repository descriptions and project-level structure, especially these repositories:

- [microsphere-projects organization](https://github.com/microsphere-projects)
- [microsphere-build](https://github.com/microsphere-projects/microsphere-build)
- [microsphere-bom](https://github.com/microsphere-projects/microsphere-bom)
- [microsphere-java](https://github.com/microsphere-projects/microsphere-java)
- [microsphere-spring](https://github.com/microsphere-projects/microsphere-spring)
- [microsphere-spring-boot](https://github.com/microsphere-projects/microsphere-spring-boot)
- [microsphere-mybatis](https://github.com/microsphere-projects/microsphere-mybatis)
- [microsphere-spring-cloud](https://github.com/microsphere-projects/microsphere-spring-cloud)
- [microsphere-resilience4j](https://github.com/microsphere-projects/microsphere-resilience4j)
- [microsphere-i18n](https://github.com/microsphere-projects/microsphere-i18n)
- [microsphere-test](https://github.com/microsphere-projects/microsphere-test)
- [microsphere-logging](https://github.com/microsphere-projects/microsphere-logging)

The organization overview also lists additional project families and repositories, including:

- `microsphere-alibaba-druid`
- `microsphere-hibernate`
- `microsphere-redis`
- `microsphere-alibaba-sentinel`
- `microsphere-gateway`
- `microsphere-multiactive`
- `microsphere-netflix`
- `microsphere-java-enterprise`

For those repositories, some notes below are necessarily higher-level inferences from project naming, organization grouping, and repository descriptions on the organization page rather than deep source review.

---

## 1. Evaluation criteria

Each Microsphere project is evaluated along these dimensions:

- What Rose can borrow structurally or functionally
- Which Rose module or theme it maps to
- Suggested action
- Priority
- Whether the change is likely to be breaking for consumers

Priority levels used in this document:

- **High**: strong fit with Rose today; can reasonably shape near-term architecture
- **Medium**: useful, but should follow after more urgent platform consolidation
- **Low**: mostly organizational inspiration or long-term optional direction

---

## 2. Layered view: from foundation to higher-level integrations

This section is ordered from the most foundational project types upward:

1. Build and dependency governance
2. Pure Java foundation
3. Spring platform extensions
4. Spring Boot platform extensions
5. Data and persistence integrations
6. Runtime, resilience, and edge integrations
7. Testing, support, and repository governance

---

## 3. Build and dependency governance

### 3.1 `microsphere-build`

**Priority:** Medium  
**Best fit for Rose:** root build governance, parent POM conventions

#### What is worth borrowing

- A dedicated shared build parent for plugin management and common build conventions
- Explicit separation between build settings and feature modules

#### Why this matters for Rose

Rose already has a strong root parent, but the build contract can become clearer as the repository grows. Microsphere treats build setup as a first-class concern rather than an afterthought.

#### Suggested Rose actions

- Keep root parent responsibilities explicit
- Continue documenting build, release, coverage, and publishing behavior separately from feature docs
- Consider whether coverage/release support modules deserve clearer governance documentation

#### Breaking change risk

- **Low** if this remains documentation and parent-POM cleanup

---

### 3.2 `microsphere-bom`

**Priority:** Medium  
**Best fit for Rose:** `rose-bom`, dependency governance

#### What is worth borrowing

- Treating BOM management as its own clearly documented product
- Making consumer import patterns extremely explicit

#### Why this matters for Rose

Rose already uses a BOM, but Microsphere reinforces the idea that BOM discipline is part of the public developer experience, not just internal build plumbing.

#### Suggested Rose actions

- Keep `rose-bom` usage prominent in README and module docs
- Document artifact rename impacts through the BOM whenever restructuring changes consumer coordinates

#### Breaking change risk

- **Low** for documentation and governance improvements

---

## 4. Pure Java foundation

### 4.1 `microsphere-java`

**Priority:** Medium  
**Best fit for Rose:** `rose-core`

#### What is worth borrowing

- A clear "common Java features" base layer that serves the rest of the ecosystem
- Strong discipline about what belongs in the pure Java foundation vs. framework integration layers

#### Why this matters for Rose

This is the closest organizational analogue to `rose-core`. The lesson is not to grow `rose-core` indiscriminately, but to keep it useful, stable, and framework-light.

#### Suggested Rose actions

- Keep `rose-core` free from avoidable Spring Boot assumptions
- Move only truly reusable, framework-light utilities into `rose-core`
- Resist the temptation to treat `rose-core` as a dumping ground

#### Breaking change risk

- **Low to Medium** depending on whether code is moved across module boundaries

---

### 4.2 `microsphere-java-enterprise`

**Priority:** Low  
**Best fit for Rose:** scope discipline

#### What is worth borrowing

- Large platform ambitions are split into explicit projects instead of being hidden inside a generic base layer

#### Why this matters for Rose

This is more a cautionary architectural lesson than a roadmap suggestion: if Rose ever broadens beyond Boot extension work, that expansion should be explicit and separately named.

#### Suggested Rose actions

- No immediate action
- Use only as a reminder to keep Rose's scope clear

#### Breaking change risk

- **Low**

---

## 5. Spring platform extensions

### 5.1 `microsphere-spring`

**Priority:** Medium-High  
**Best fit for Rose:** `rose-spring-boot`, `rose-multitenancy`, future test support

#### What is worth borrowing

- Clear thematic separation for Spring extensions: context, web, webmvc, jdbc, test
- Configuration source and property binding enhancement ideas
- Stronger test-support modularization
- A pattern of shared abstractions plus specialization modules

#### Why this matters for Rose

Rose currently stays deliberately narrow, which is good. But as features expand, it will likely need:

- more reusable config-handling infrastructure
- clearer separation between shared web logic and MVC-specific logic
- lighter-weight ways to provide test utilities across multiple themes

#### Suggested Rose actions

- Keep `rose-multitenancy-web` focused and consider shared-vs-MVC boundaries before it grows further
- Revisit config source extension only if multiple themes start solving similar binding or loading problems
- Consider theme-level `*-tests` modules before introducing a full repository-wide test platform

#### Breaking change risk

- Usually **non-breaking** if introduced as new internal modules
- Can become breaking if package structure or existing public APIs are reorganized too aggressively

---

### 5.2 `microsphere-i18n`

**Priority:** Low  
**Best fit for Rose:** module-boundary judgment

#### What is worth borrowing

- Independent, focused themes should remain independent rather than being absorbed into unrelated base modules

#### Why this matters for Rose

This is mostly a governance lesson: when a capability has its own user value and growth path, treat it as a proper module or theme.

#### Suggested Rose actions

- Use as a lightweight rule of thumb for future theme extraction
- Do not act on it immediately unless a new independent capability appears

#### Breaking change risk

- **Non-breaking** as a planning principle

---

## 6. Spring Boot platform extensions

### 6.1 `microsphere-spring-boot`

**Priority:** High  
**Best fit for Rose:** `rose-spring-boot`, future actuator/diagnostics work

#### What is worth borrowing

- A stronger platform core layer that collects reusable Boot extensions in one place
- Unified handling for default properties and environment customization
- More systematic diagnostics and startup troubleshooting
- Actuator-related enhancements treated as a distinct concern instead of scattered features

#### Why this matters for Rose

Rose already has pieces of this direction:

- bootstrap environment post-processing
- dev/test mode profile activation
- environment property bridging in OpenTelemetry integration
- failure analyzers in observation and dev-services

The opportunity is to make these feel like one coherent platform layer rather than several local solutions.

#### Suggested Rose actions

- Strengthen `rose-spring-boot` as the home for shared Boot infrastructure
- Introduce a unified default-configuration loading mechanism
- Generalize environment and auto-configuration exclusion helpers where practical
- Evaluate a more explicit actuator/diagnostics story for Rose-owned runtime features

#### Breaking change risk

- Mostly **non-breaking** if introduced as additive infrastructure
- Could become breaking if existing property behavior is changed implicitly

---

### 6.2 `microsphere-logging`

**Priority:** Low-Medium  
**Best fit for Rose:** `rose-opentelemetry-logback-bridge`, future logging integrations

#### What is worth borrowing

- Treating logging as a real integration area instead of a side detail
- Leaving room for logging-specific extensions to evolve independently

#### Why this matters for Rose

Rose already has logging-related OpenTelemetry bridging. If that area expands, Microsphere's dedicated logging project is a reminder that logging can justify its own design space.

#### Suggested Rose actions

- Keep logging concerns visible in the OpenTelemetry theme
- Reassess whether future logging features should stay under `rose-opentelemetry` or become a broader theme only if they outgrow telemetry-specific concerns

#### Breaking change risk

- **Low** if used as a design heuristic only

---

### 6.3 `microsphere-spring-cloud`

**Priority:** Medium  
**Best fit for Rose:** documentation, release strategy, future compatibility policy

#### What is worth borrowing

- Clear compatibility-matrix documentation
- Explicit explanation of supported versions, branches, and activation profiles
- Strong consumer-facing documentation for runtime integration choices

#### Why this matters for Rose

Rose is currently focused on Boot 2.7 / Java 8. That is clear now, but long-lived platform libraries eventually need sharper compatibility communication, especially if future Boot 3 / Jakarta work begins.

#### Suggested Rose actions

- Add a version compatibility matrix to README or docs
- Make branch/release expectations explicit if multiple support lines ever appear
- Keep runtime integration guidance crisp, especially for OpenTelemetry and dev-services

#### Breaking change risk

- **Non-breaking**; mostly documentation and release-process improvement

---

## 7. Data and persistence integrations

### 7.1 `microsphere-mybatis`

**Priority:** High  
**Best fit for Rose:** `rose-mybatis-plus`

#### What is worth borrowing

- Treating MyBatis integration as a dedicated theme rather than a loose utility module
- SQL statement interception as a first-class extension point
- Building a capability family around persistence integration instead of one-off custom hooks

#### Why this matters for Rose

You have already identified that `rose-mybatis-plus` should have submodules. That makes it a natural theme directory. Once that happens, the next architectural question is whether Rose wants:

- SQL auditing
- data-permission processing chains
- encryption/decryption pipelines
- metrics or tracing around SQL execution

Microsphere's MyBatis direction suggests this should be modeled as a coherent extension surface, not a pile of unrelated interceptors.

#### Suggested Rose actions

- Formalize `rose-mybatis-plus` as a theme directory
- Use `core + starter` as the baseline submodule shape
- Evaluate whether SQL interception should become an explicit internal extension pipeline

#### Breaking change risk

- Directory-only changes can be **non-breaking**
- Artifact renames like `rose-data-mybatis-plus` -> `rose-mybatis-plus-core` are **breaking**

---

### 7.2 `microsphere-hibernate`

**Priority:** Low  
**Best fit for Rose:** future persistence strategy only

#### What is worth borrowing

- A separate persistence-integration theme for a major ORM technology

#### Why this matters for Rose

This is mainly a naming and boundary lesson. If Rose ever adds JPA/Hibernate support, it should likely be a distinct theme rather than being folded into SQLite or MyBatis modules.

#### Suggested Rose actions

- Do nothing now unless JPA/Hibernate becomes a real roadmap topic
- Use this as a theme-boundary precedent only

#### Breaking change risk

- **Low** as a planning principle

---

### 7.3 `microsphere-redis`

**Priority:** Low-Medium  
**Best fit for Rose:** theme extraction heuristics, not immediate implementation

#### What is worth borrowing

- Recognizing when a data/store integration deserves an independent theme

#### Why this matters for Rose

Rose currently touches Redis via dev-services, not as an application-facing capability theme. If that changes in the future, Redis should probably grow as its own theme rather than being hidden under a generic storage bucket.

#### Suggested Rose actions

- Keep Redis under `rose-dev` for now
- Revisit only if Rose starts offering application-facing Redis helpers or integrations

#### Breaking change risk

- **Low**

---

### 7.4 `microsphere-alibaba-druid`

**Priority:** Low  
**Best fit for Rose:** possible future data-access integration theme

#### What is worth borrowing

- Integrating a specific infrastructure technology as its own explicit project rather than burying it in a generic utilities module

#### Why this matters for Rose

This is another useful precedent for future expansion, but Rose currently does not appear to need a Druid-like direction.

#### Suggested Rose actions

- No immediate action
- Use only as a guide for future theme extraction decisions

#### Breaking change risk

- **Low**

---

## 8. Runtime, resilience, and edge integrations

### 8.1 `microsphere-resilience4j`

**Priority:** Low-Medium  
**Best fit for Rose:** theme-splitting heuristics, not immediate feature work

#### What is worth borrowing

- The idea that a third-party ecosystem integration can justify a standalone theme family
- A pattern for growing one topic into multiple integration modules over time

#### Why this matters for Rose

This is less about adding resilience features now and more about recognizing when a Rose capability should become a first-class theme rather than staying buried inside another area.

#### Suggested Rose actions

- Use this as an organizational precedent, not an implementation target
- Promote a capability to a theme directory only when it has multiple real submodules or clear extension points

#### Breaking change risk

- Usually **non-breaking** if applied only as a design rule

---

### 8.2 `microsphere-alibaba-sentinel`

**Priority:** Low  
**Best fit for Rose:** future resilience theme planning

#### What is worth borrowing

- Fault-tolerance technologies can be organized as independent integration themes

#### Why this matters for Rose

This reinforces the same lesson as `microsphere-resilience4j`: if Rose ever enters the resilience space, it should do so intentionally as a theme, not as scattered utility classes.

#### Suggested Rose actions

- No immediate action
- Keep as a precedent only

#### Breaking change risk

- **Low**

---

### 8.3 `microsphere-gateway`

**Priority:** Low-Medium  
**Best fit for Rose:** future edge/gateway integrations only

#### What is worth borrowing

- Edge-layer concerns may deserve a distinct project family instead of being hidden under generic web modules

#### Why this matters for Rose

Rose currently has no gateway theme, so this is not a direct implementation target. It is mainly useful as a reminder that some integration concerns grow sideways rather than fitting inside core web support.

#### Suggested Rose actions

- No immediate action
- Use as a theme-boundary precedent if edge/gateway features ever appear

#### Breaking change risk

- **Low**

---

### 8.4 `microsphere-multiactive`

**Priority:** Low-Medium  
**Best fit for Rose:** multitenancy and distributed deployment thinking

#### What is worth borrowing

- Treating advanced distributed deployment concerns as dedicated integration areas

#### Why this matters for Rose

This is not a direct fit today, but conceptually it is adjacent to `rose-multitenancy`: both involve contextual routing, environment-aware behavior, and cross-cutting runtime policy.

#### Suggested Rose actions

- No direct implementation now
- Keep in mind if multitenancy grows into broader routing, isolation, or regional behavior features

#### Breaking change risk

- **Low**

---

### 8.5 `microsphere-netflix`

**Priority:** Low  
**Best fit for Rose:** historical integration-pattern reference only

#### What is worth borrowing

- Legacy ecosystem integrations may be better isolated as their own theme family

#### Why this matters for Rose

Mostly a structural lesson. Even if Rose never integrates Netflix OSS technologies, the repository naming shows a disciplined approach to ecosystem boundaries.

#### Suggested Rose actions

- No immediate action

#### Breaking change risk

- **Low**

---

## 9. Testing, support, and repository governance

### 9.1 `microsphere-test`

**Priority:** Medium  
**Best fit for Rose:** future testing support strategy

#### What is worth borrowing

- Treating test support as a reusable layer rather than duplicating fixtures in many modules
- Providing shared test infrastructure as its own maintainable unit

#### Why this matters for Rose

Rose already has significant test coverage distributed across modules. As cross-module integration grows, especially around starters and dev-services, shared test support may become more important.

#### Suggested Rose actions

- Start with theme-local `*-tests` modules where needed
- Reassess a repository-wide `rose-test` only after duplication becomes obvious

#### Breaking change risk

- Usually **non-breaking** if additive

---

### 9.2 Organization-level project families

**Priority:** High  
**Best fit for Rose:** repository structure, naming, roadmap governance

#### What is worth borrowing

- A strong rule that only real module families get grouped
- Distinct themes with clear naming and independent evolution
- Making the repository legible from the top-level directory alone

#### Why this matters for Rose

This aligns directly with the repository restructuring already under discussion:

- keep `rose-opentelemetry`, `rose-multitenancy`, `rose-dev`
- introduce `rose-mybatis-plus` as a real theme directory
- avoid a weak `rose-data/` umbrella

#### Suggested Rose actions

- Continue the planned module restructuring
- Keep single-purpose modules flat at root
- Group only real theme families

#### Breaking change risk

- Directory changes alone are **non-breaking**
- Artifact and module path changes may be **breaking** for contributors or consumers

---

## 10. Microsphere to Rose action table

| Layer | Microsphere project | Borrowing focus | Rose target | Suggested action | Priority | Breaking risk |
|-------|---------------------|-----------------|-------------|------------------|----------|---------------|
| Build | `microsphere-build` | shared build parent and build governance | root build setup | keep build responsibilities explicit | Medium | Low |
| Build | `microsphere-bom` | BOM as public developer contract | `rose-bom` | strengthen BOM-facing documentation | Medium | Low |
| Foundation | `microsphere-java` | pure Java foundation layer | `rose-core` | keep base utilities framework-light | Medium | Low to Medium |
| Foundation | `microsphere-java-enterprise` | scope and project-family discipline | long-term scope governance | use only as cautionary precedent | Low | Low |
| Spring | `microsphere-spring` | Spring extension layering, config support, test support | `rose-spring-boot`, `rose-multitenancy` | adopt selectively as themes grow | Medium-High | Low to Medium |
| Spring | `microsphere-i18n` | module-boundary discipline | future theme planning | use as a planning heuristic only | Low | Low |
| Boot | `microsphere-spring-boot` | platform core, default config, diagnostics, actuator structure | `rose-spring-boot` | consolidate shared Boot infrastructure | High | Low to Medium |
| Boot | `microsphere-logging` | logging as a distinct integration area | logging and telemetry modules | grow only if logging concerns expand | Low-Medium | Low |
| Boot | `microsphere-spring-cloud` | compatibility matrices, version documentation | docs, release strategy | improve compatibility and support docs | Medium | Low |
| Data | `microsphere-mybatis` | MyBatis theme design, SQL interception | `rose-mybatis-plus` | formalize theme and evaluate extension pipeline | High | Medium to High |
| Data | `microsphere-hibernate` | separate persistence theme for ORM tech | future persistence themes | use as precedent only | Low | Low |
| Data | `microsphere-redis` | independent store/integration theme | future Redis-facing features | keep under dev-services unless scope expands | Low-Medium | Low |
| Data | `microsphere-alibaba-druid` | explicit technology-specific integration | future data-access themes | no immediate action | Low | Low |
| Runtime | `microsphere-resilience4j` | theme extraction heuristic | architecture governance | use as precedent for future theme splitting | Low-Medium | Low |
| Runtime | `microsphere-alibaba-sentinel` | dedicated fault-tolerance integration theme | future resilience themes | no immediate action | Low | Low |
| Runtime | `microsphere-gateway` | edge/gateway as distinct theme | future gateway features | no immediate action | Low-Medium | Low |
| Runtime | `microsphere-multiactive` | advanced distributed runtime concerns | future multitenancy evolution | keep as conceptual reference | Low-Medium | Low |
| Runtime | `microsphere-netflix` | explicit ecosystem boundary | future legacy integrations | no immediate action | Low | Low |
| Governance | `microsphere-test` | reusable testing infrastructure | theme tests, possible `rose-test` | add theme-local test support first | Medium | Low |
| Governance | organization structure | theme families and naming clarity | whole repository | continue restructure toward real theme grouping | High | Medium |

---

## 11. Recommended priority order for Rose

### Immediate

1. Continue repository restructuring around real themes
2. Strengthen `rose-spring-boot` as a shared Boot infrastructure layer
3. Formalize `rose-mybatis-plus` as a theme directory

### Near-term

1. Add a unified compatibility/documentation section for consumers
2. Improve diagnostics and runtime visibility
3. Add theme-local README files where module families are already large

### Later

1. Reassess shared test infrastructure after more cross-module integration appears
2. Revisit broader Spring extension utilities only if multiple themes converge on the same need
3. Consider new theme extraction only when a capability has real independent growth

---

## 12. Non-goals

Rose should **not** use this benchmark as a reason to:

- copy Microsphere feature-by-feature
- expand into unrelated ecosystems too early
- introduce WebFlux, Guice, Spring Cloud, or resilience integrations without a concrete Rose need
- mix large structural migration with unrelated behavior changes

The main lesson is not "become Microsphere." The main lesson is:

- keep platform infrastructure coherent
- make theme boundaries explicit
- grow only where Rose already has a real product direction
