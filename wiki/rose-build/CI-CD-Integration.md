# CI/CD Integration

Rose uses GitHub Actions workflows in `.github/workflows/`.

---

## Workflows

| Workflow | Trigger | Purpose |
|---|---|---|
| [`ci.yml`](../../.github/workflows/ci.yml) | Push/PR to `main` | Enforcer, build, unit + ITs, JaCoCo, Codecov |
| [`publish.yml`](../../.github/workflows/publish.yml) | Tag `v*` / manual | Deploy to Maven Central |
| [`wiki.yml`](../../.github/workflows/wiki.yml) | Push to `main` (`wiki/**`) | Sync `wiki/` → GitHub Wiki |

---

## CI (`ci.yml`)

Two jobs (fast failure first):

| Job | Command | Purpose |
|-----|---------|---------|
| `enforce-project-rules` | `./mvnw -B -ntp validate` | Maven enforcer (Maven/Java version, banned deps, duplicate versions) |
| `build-and-test` | `./mvnw -B -ntp -Pcoverage verify` | Compile, Surefire unit tests, Failsafe `*IT`, JaCoCo reports |

After tests:

- Artifact `jacoco-report` (`**/target/site/jacoco/jacoco.xml`, `**/target/site/jacoco-it/jacoco.xml`, 14 days)
- Upload to Codecov (`codecov/codecov-action`, `fail_ci_if_error: false`)

**Runtime:** Temurin JDK 8, Maven Wrapper (`mvnw`, Maven 3.9.16), Maven dependency cache via `actions/setup-java`.

**Requirements for green CI:**

- Docker on the runner (Testcontainers integration tests in `rose-devservice` and elsewhere).
- Fork PRs do not need repository secrets; `CODECOV_TOKEN` is optional on public repos.

---

## Maven Publish (`publish.yml`)

Separate from CI. Credentials via `actions/setup-java` (`server-id: central`, matching `publishingServerId` in `rose-build/pom.xml`).

| Trigger | Behavior |
|---|---|
| Push tag `v1.0.0` | Release `1.0.0` with GPG signing |
| `workflow_dispatch` + `release_version` | Manual release of that version |
| `workflow_dispatch`, version empty | Publish current SNAPSHOT (`-Dgpg.skip=true`) |

Command: `./mvnw -B -ntp -Prelease -DskipTests deploy` (add `-Drevision=…` for releases).

See [Profiles-Management](Profiles-Management) for the `release` profile.

---

## Wiki Publish

Markdown under `wiki/` (grouped by module subfolders) is rsync'd to the repository's GitHub Wiki on every push to `main` that touches `wiki/**`.

**First-time setup:** create at least one page in the GitHub Wiki UI before the workflow can push.

Local edit workflow:

1. Edit `wiki/<module>/*.md` in this repository
2. Push to `main`
3. Workflow syncs to https://github.com/zhijun-io/rose/wiki

---

## Secrets

Use these **exact** secret names (org- or repo-level) so workflows stay consistent across repositories:

| Secret | Used by |
|---|---|
| `CODECOV_TOKEN` | Codecov upload in `ci.yml` (optional on public repos) |
| `MAVEN_USERNAME` | Sonatype Central portal token username (`publish.yml`) |
| `MAVEN_CENTRAL_TOKEN` | Sonatype Central portal token password (`publish.yml`) |
| `MAVEN_GPG_PRIVATE_KEY` | GPG private key for signed releases (`publish.yml`) |
| `MAVEN_GPG_PASSPHRASE` | GPG key passphrase (`publish.yml`) |

---

## Local CI Parity

```bash
# Match CI enforcer
./mvnw -B -ntp validate

# Match CI full verify (needs Docker)
./mvnw -B -ntp -Pcoverage verify

# Faster local loop
./mvnw test
./mvnw verify -DskipITs

# Optional local JaCoCo only
./mvnw verify -Pcoverage
```
