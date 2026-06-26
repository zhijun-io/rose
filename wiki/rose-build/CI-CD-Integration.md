# CI/CD Integration

Rose uses GitHub Actions workflows in `.github/workflows/`, inspired by [microsphere-build](https://github.com/microsphere-projects/microsphere-build/wiki/CI-CD-Integration).

---

## Workflows

| Workflow | Trigger | Purpose |
|---|---|---|
| [`maven-build.yml`](../../.github/workflows/maven-build.yml) | Push/PR to `main` | Multi-JDK unit + ITs + coverage + CodeQL |
| [`maven-publish.yml`](../../.github/workflows/maven-publish.yml) | Release tag / manual | Deploy to Maven Central |
| [`publish-wiki.yml`](../../.github/workflows/publish-wiki.yml) | Push to `main` (`wiki/**`) | Sync `wiki/` → GitHub Wiki |

---

## Maven Build

Four parallel jobs (see workflow header in `maven-build.yml`):

| Job | JDK | Command | Purpose |
|-----|-----|---------|---------|
| `unit` | 8, 11, 17, 21 | `mvn -B -ntp verify -DskipITs` | Compile + unit tests (`fail-fast: false`) |
| `integration` | 8 | `mvn -B -ntp verify` | Full `*IT` on minimum JDK (Docker) |
| `coverage` | 25 | `mvn -B -ntp verify -Pcoverage` | IT + JaCoCo → Codecov; JaCoCo artifacts retained 7 days |
| `codeql` | 25 | `compile` + `analyze` | Security (not coverage) |

**Requirements for green CI:**

- Docker available on the runner (integration tests and dev-service ITs).
- No duplicate POM dependency declarations (enforced by enforcer).

---

## Maven Publish

Release flow (see [Profiles-Management](Profiles-Management)):

1. Tag release from `main`
2. `mvn deploy -Prelease` with Central Portal credentials and GPG
3. Bump `${revision}` to next SNAPSHOT

`<revision>` is read from `rose-build/pom.xml`; CI may pass `-Drevision=…`.

---

## Wiki Publish

Markdown under `wiki/` (grouped by module subfolders) is rsync'd to the repository's GitHub Wiki on every push to `main`.

**First-time setup:** create at least one page in the GitHub Wiki UI before the workflow can push.

Local edit workflow:

1. Edit `wiki/<module>/*.md` in this repository
2. Push to `main`
3. Workflow syncs to https://github.com/zhijun-io/rose/wiki

---

## Secrets

| Secret | Used by |
|---|---|
| `CODECOV_TOKEN` | Codecov upload (`coverage` job) |
| `SONAR_TOKEN` | SonarCloud (if configured) |
| Central Portal / GPG | Release workflow |

---

## Local CI Parity

```bash
# Match CI full verify (needs Docker)
mvn -B verify

# Faster local loop
mvn test
mvn verify -DskipITs

# Optional local JaCoCo
mvn verify -Pcoverage
```
