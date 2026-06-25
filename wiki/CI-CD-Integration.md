# CI/CD Integration

Rose uses GitHub Actions workflows in `.github/workflows/`, inspired by [microsphere-build](https://github.com/microsphere-projects/microsphere-build/wiki/CI-CD-Integration).

---

## Workflows

| Workflow | Trigger | Purpose |
|---|---|---|
| [`maven-build.yml`](../.github/workflows/maven-build.yml) | Push/PR to `main` | Multi-JDK build and test |
| [`maven-publish.yml`](../.github/workflows/maven-publish.yml) | Release tag / manual | Deploy to Maven Central |
| [`publish-wiki.yml`](../.github/workflows/publish-wiki.yml) | Push to `main` (`wiki/**`) | Sync `wiki/` → GitHub Wiki |

---

## Maven Build

**Matrix:** JDK 8, 11, 17, 21, 25 on `ubuntu-latest`

**Commands:**

| JDK | Command |
|-----|---------|
| 8 | `mvn -B -U -ntp verify` (unit + integration tests) |
| 11, 17, 25 | `mvn -B -U -ntp verify -DskipITs` (unit tests only) |
| 21 | `mvn -B -U -ntp verify -Pcoverage` (unit + IT + JaCoCo aggregate gate) |

- JaCoCo coverage and the 35% aggregate gate run **only** on JDK 21.
- Codecov upload runs on JDK 21 when `CODECOV_TOKEN` is configured.

**Requirements for green CI:**

- Docker available on the runner (integration tests and dev-service ITs).
- No duplicate POM dependency declarations (enforced by enforcer).

---

## Maven Publish

Release flow (see [rose-build/README.md](../rose-build/README.md)):

1. Tag release from `main`
2. `mvn deploy -Prelease` with Central Portal credentials and GPG
3. Bump `${revision}` to next SNAPSHOT

`<revision>` is read from `rose-build/pom.xml`; CI may pass `-Drevision=…`.

---

## Wiki Publish

Markdown files under `wiki/` are rsync'd to the repository's GitHub Wiki on every push to `main`.

**First-time setup:** create at least one page in the GitHub Wiki UI before the workflow can push.

Local edit workflow:

1. Edit `wiki/*.md` in this repository
2. Push to `main`
3. Workflow syncs to https://github.com/zhijun-io/rose/wiki

---

## Secrets

| Secret | Used by |
|---|---|
| `CODECOV_TOKEN` | Codecov upload (optional) |
| `SONAR_TOKEN` | SonarCloud (if configured) |
| Central Portal / GPG | Release workflow |

---

## Local CI Parity

```bash
# Match CI build (needs Docker)
mvn -B verify -Pcoverage

# Faster local loop
mvn test
mvn verify -DskipITs
```
