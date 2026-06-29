# Repository Guidelines

Operating guide for AI agents in **zhijun-io/rose**. Human overview: [README.md](README.md). Detailed implementation conventions: [docs/rose-conventions.md](docs/rose-conventions.md). Build and CI reference: [wiki/rose-build/CI-CD-Integration.md](wiki/rose-build/CI-CD-Integration.md).

## Context

- Rose is a **Spring Boot 2.7 / Java 8+** extension platform published under `io.zhijun`.
- This repository is a **Maven multi-module reactor**. Consumer applications should import **`rose-bom`**; they should not use `rose-build` or `rose-parent` as the application parent.
- The codebase is organized around `core` / `spring` / `spring-boot` layers. `-spring-boot` modules own auto-configuration and starters.
- Treat package paths containing `.internal.` as non-public implementation detail.
- Keep root guidance high-level; module-specific conventions live in the code and [docs/rose-conventions.md](docs/rose-conventions.md). A single root `AGENTS.md` is enough here.

## Structure

| Path | Purpose |
| --- | --- |
| `rose-build/` | Shared parent/build logic, dependency policy, release profiles |
| `rose-bom/` | Consumer BOM |
| `rose-foundation/` | Annotations, annotation processor, shared test support |
| `rose-spring/` | Spring Framework integrations without Boot auto-configuration |
| `rose-spring-boot/` | Baseline Boot starters and actuator support |
| `rose-observation/` | OpenTelemetry, logback, OTLP, Micrometer bridge starters |
| `rose-multitenancy/` | Multitenancy core, Spring, Boot modules |
| `rose-mybatis-plus/` | MyBatis-Plus core, Spring, Boot modules |
| `rose-devservice/` | Testcontainers-based dev-service modules and test support |
| `docs/design/` | Design specs and implementation plans |
| `wiki/` | User-facing docs mirrored to the GitHub Wiki on push to `main` |

## Commands

Run the exact Maven commands the repository and CI already use:

```bash
./mvnw -B -ntp validate
./mvnw -B -ntp test
./mvnw -B -ntp verify -DskipITs
./mvnw -B -ntp verify
./mvnw -B -ntp -Pcoverage verify
```

- CI unit matrix runs `validate` and `verify -DskipITs` on Java 8, 11, 17, 21, and 25.
- CI integration runs `-Pcoverage verify` on Java 17.
- Use `test` for narrow unit-only work, `verify -DskipITs` for cross-module or auto-configuration changes, and full `verify` when `*IT`, Testcontainers, or dev-service behavior may be affected.
- Do not invent Gradle, npm, or pytest commands; this repository is Maven-only.

## Commit & PR

- Commit messages should follow **Chris Beams** style and use repo conventions such as `feat:`, `fix:`, `docs:`, `chore:`, or `refactor:`.
- Keep one logical change per commit and one topic per PR.
- PRs should answer: **What changed?**, **Why?**, **Breaking changes?** (use `None` if there are none), and **How was it verified?**
- Record user-visible changes in [CHANGELOG.md](CHANGELOG.md).
- Update docs when behavior, configuration, compatibility, or usage changes. In particular, changes under `wiki/` will sync to the GitHub Wiki on push to `main`.

## Agent Notes

- Prefer existing third-party APIs over new `*Utils`, `*Helper`, or `*Support` abstractions.
- For every `-spring-boot` module, keep `META-INF/spring.factories` and `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` in sync when adding or changing auto-configurations.
- Add or update tests in the same module when public behavior changes. `*IT` covers integration behavior and does not replace unit tests.
- The working tree may already be dirty. Do not revert or overwrite unrelated user changes.
- Do not push to `main`, force-push protected branches, change git config, or create commits unless the user explicitly asks.
