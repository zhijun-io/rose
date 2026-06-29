# CI/CD Integration

Rose uses GitHub Actions workflows in `.github/workflows/`.

---

## Workflows

| Workflow                                             | Trigger                    | Purpose                                      |
|------------------------------------------------------|----------------------------|----------------------------------------------|
| [`ci.yml`](../../.github/workflows/ci.yml)           | Push/PR to `main`          | Validate, layered unit + ITs, coverage, Codecov |
| [`publish.yml`](../../.github/workflows/publish.yml) | Tag `v*` / manual          | Deploy to Maven Central                      |
| [`wiki.yml`](../../.github/workflows/wiki.yml)       | Push to `main` (`wiki/**`) | Sync `wiki/` → GitHub Wiki                   |

---

## CI (`ci.yml`)

The workflow is layered so pull requests get faster feedback while `main` keeps the full compatibility sweep.

| Trigger        | Job                | JDK                    | Command                            | Purpose                                           |
|----------------|--------------------|------------------------|------------------------------------|---------------------------------------------------|
| PR / `main`    | `validate`         | **17**                 | `./mvnw -B -ntp validate`          | Enforcer and baseline build validation            |
| `pull_request` | `unit-pr`          | **8, 17, 21** (matrix) | `./mvnw -B -ntp verify -DskipITs`  | Fast unit coverage across baseline/current LTS    |
| `pull_request` | `integration-pr`   | **17**                 | `./mvnw -B -ntp -DskipSurefireTests=true verify` | `*IT`, Testcontainers, auto-configuration checks without rerunning Surefire |
| `push` `main`  | `unit-main`        | **8, 11, 17, 21, 25**  | `./mvnw -B -ntp verify -DskipITs`  | Full compatibility unit matrix                    |
| `push` `main`  | `integration-main` | **17**                 | `./mvnw -B -ntp -Pcoverage verify` | Full integration suite, JaCoCo, and Codecov       |

After `integration-main`:

- Artifact `jacoco-report-java-17` (`**/target/site/jacoco/jacoco.xml`, `**/target/site/jacoco-it/jacoco.xml`, 14 days)
- Upload to Codecov (`codecov/codecov-action`, `fail_ci_if_error: false`)

**Runtime:** Temurin JDK (see matrix above), Maven Wrapper (`mvnw`), Maven dependency cache via `actions/setup-java`.

**Path filtering:** pure documentation changes in `docs/**`, `wiki/**`, `README.md`, and `CHANGELOG.md` skip `ci.yml`.

See also: [Compatibility Matrix](Compatibility-Matrix) for supported Java / Boot / Testcontainers versions.

**Requirements for green CI:**

- Docker on the runner (Testcontainers integration tests in `rose-devservice` and elsewhere).
- Fork PRs do not need repository secrets; `CODECOV_TOKEN` is optional on public repos.

---

## Maven Publish (`publish.yml`)

Separate from CI. Credentials via `actions/setup-java` (`server-id: central`, matching `publishingServerId` in
`rose-build/pom.xml`).

| Trigger                                 | Behavior                                     |
|-----------------------------------------|----------------------------------------------|
| Push tag `v1.0.0`                       | Release `1.0.0` with GPG signing             |
| `workflow_dispatch` + `release_version` | Manual release of that version               |
| `workflow_dispatch`, version empty      | Publish current SNAPSHOT (`-Dgpg.skip=true`) |

Command: `./mvnw -B -ntp -Prelease -DskipTests deploy` (add `-Drevision=…` for releases).

See [Profiles-Management](Profiles-Management) for the `release` profile.

---

## Wiki Publish

Markdown under `wiki/` (grouped by module subfolders) is rsync'd to the repository's GitHub Wiki on every push to `main`
that touches `wiki/**`.

**First-time setup:** create at least one page in the GitHub Wiki UI before the workflow can push.

Local edit workflow:

1. Edit `wiki/<module>/*.md` in this repository
2. Push to `main`
3. Workflow syncs to https://github.com/zhijun-io/rose/wiki

---

## Secrets

Use these **exact** secret names (org- or repo-level) so workflows stay consistent across repositories:

| Secret                  | Used by                                                |
|-------------------------|--------------------------------------------------------|
| `CODECOV_TOKEN`         | Codecov upload in `ci.yml` (optional on public repos)  |
| `MAVEN_USERNAME`        | Sonatype Central portal token username (`publish.yml`) |
| `MAVEN_CENTRAL_TOKEN`   | Sonatype Central portal token password (`publish.yml`) |
| `MAVEN_GPG_PRIVATE_KEY` | GPG private key for signed releases (`publish.yml`)    |
| `MAVEN_GPG_PASSPHRASE`  | GPG key passphrase (`publish.yml`)                     |

---

## Local CI Parity

```bash
# Match CI validate
./mvnw -B -ntp validate

# Match PR integration (needs Docker)
./mvnw -B -ntp -DskipSurefireTests=true verify

# Match main branch coverage integration (needs Docker)
./mvnw -B -ntp -Pcoverage verify

# Match CI unit jobs
./mvnw test
./mvnw verify -DskipITs
```
