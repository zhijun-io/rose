# Contributing to Rose

Thank you for contributing to Rose (`io.zhijun`).

## Development principles

Read **[docs/development-principles.md](docs/development-principles.md)** before substantial changes. Core ideas:

- **Do not reinvent the wheel** — prefer Spring Boot, OpenTelemetry, Testcontainers, and existing Rose modules.
- **Extend Boot, do not replace it** — applications import `rose-bom`; they do not inherit `rose-parent`.
- **Compose before you duplicate** — reuse capabilities and starters; keep changes minimal and focused.

Structural rules are in **[docs/module-layering.md](docs/module-layering.md)** (mandatory for new modules).

## Prerequisites

- JDK 8
- Maven 3.6+
- Docker (OrbStack, Docker Desktop, or compatible runtime for dev services and integration tests)

## Build and test

```bash
mvn validate
mvn compile test-compile    # no Docker required
mvn test                    # integration tests need Docker
mvn verify -Pcoverage       # JaCoCo aggregate + line coverage check (CI)
```

CI (`.github/workflows/maven-ci.yml`) calls [zhijun-io/workflows](https://github.com/zhijun-io/workflows) reusable `maven-ci.yml` — `mvn clean verify -Pcoverage` on Java 8 with Docker available for Testcontainers ITs.

## Module layering (required)

All new modules, starters, and dependency changes **must** follow the normative specification:

**[docs/module-layering.md](docs/module-layering.md)**

Summary:

- **Capabilities** (`rose-{feature}/*`) stay library-thin: prefer `rose-core` + fine-grained Spring/Boot artifacts; **no** `spring-boot-starter*` at compile scope.
- **Starters** (`rose-*-spring-boot-starter`) are POM-only aggregates that build on `rose-spring-boot-starter` and own the runnable stack (Web, Actuator, JDBC pool, …).
- **`rose-spring-boot`** is only for bootstrap integration (today: `rose-dev-services-core`), not for ordinary features.
- New published artifacts go into **both** `rose-parent` and `rose-bom` `dependencyManagement`.

PR reviewers should use the checklist in `docs/module-layering.md` §6.

## Code style

- Match existing module structure and naming (`rose-*`, package `io.zhijun.*`).
- Configuration prefix: `rose.*`.
- Auto-configuration: `META-INF/spring.factories` (not `AutoConfiguration.imports`).
- Prefer minimal, focused changes; add integration tests for dev service connectors.

## Commit messages

Use Conventional Commits, for example:

- `feat(dev-services): add mqtt connector`
- `fix(core): correct docker socket detection`
- `docs: update sample readme`

## Pull requests

- One logical change per PR when possible.
- Link related issues when applicable.
- Update `README.md` or sample docs when behavior or dependencies change.
- For new/changed modules: confirm [module layering](docs/module-layering.md) checklist (§6) in the PR description.

## Dependency updates

Maven and GitHub Actions bumps are managed by **Renovate** (`renovate.json`). Rules enforce **Spring Boot 2.7 / Java 8** guardrails (no Spring Boot 3+, Jakarta EE, Tomcat 10+, Hibernate 6+, etc.). When changing ignore rules, update `renovate.json` only.

## Releasing to Maven Central (Sonatype)

**Step-by-step release guide:** **[docs/releasing.md](docs/releasing.md)** (正式版 / snapshot、GPG、checklist).

Rose publishes to [Maven Central](https://central.sonatype.com/) via the [Central Publisher Portal](https://central.sonatype.org/publish/publish-portal-maven/) and `${revision}` versioning with `flatten-maven-plugin`.

### Prerequisites

1. A registered **`io.zhijun`** namespace on [Central Portal](https://central.sonatype.com/publishing/namespaces) (verified).
2. For **`-SNAPSHOT` deploys**: namespace menu → **Enable SNAPSHOTs** ([doc](https://central.sonatype.org/publish/publish-portal-snapshots/#enabling-snapshot-releases-for-your-namespace)). Without this, `mvn deploy` returns **`403 Forbidden`** on `maven-snapshots`.
3. A [Portal user token](https://central.sonatype.org/publish/generate-portal-token/) in `~/.m2/settings.xml` — server id **`central`** (`username` / `password` = token credentials).
4. GPG key on a public keyserver ([signing requirements](https://central.sonatype.org/publish/requirements/#sign-files-with-gpgpgp)).

Avoid `altSnapshotDeploymentRepository` or corporate Nexus mirrors in the same `settings.xml` profile used for Central deploy.

### Recommended deploy command

```bash
# ~/.m2/settings.xml must define <server><id>central</id>…</server>
mvn -B clean deploy -Prelease
```

If a corporate Maven profile redirects snapshots to an internal Nexus, disable it for Central deploy (for example `-P!your-corporate-profile`).

`rose-coverage` is **not** in the default reactor (only `mvn verify -Pcoverage`). Deploy never builds or publishes it.

### Snapshot deploy

With `<revision>…-SNAPSHOT</revision>` and SNAPSHOTs enabled on the namespace:

```bash
mvn -B clean deploy -Prelease
```

### Release deploy

1. Set `<revision>` to the release version **without** `-SNAPSHOT` (e.g. `0.1.0`).
2. Commit, tag (`git tag v0.1.0`), and push the tag.
3. `mvn -B clean deploy -Prelease`
4. Bump `<revision>` to the next snapshot (e.g. `0.2.0-SNAPSHOT`) on `main`.

The `release` profile attaches **sources**, **javadoc**, **GPG signatures**, and uploads via `central-publishing-maven-plugin` (`autoPublish=true`).

### GitHub Actions publish

| Workflow | File | When |
|----------|------|------|
| Snapshot | `.github/workflows/maven-snapshot.yml` | Push to `main` with `-SNAPSHOT` `<revision>`, or manual dispatch |
| Release | `.github/workflows/maven-release.yml` | Manual dispatch (version input) |

Requires org/repo secrets: `MAVEN_USERNAME`, `MAVEN_PASSWORD`, `GPG_SECRET_KEY`, `GPG_PASSPHRASE`. See **[docs/releasing.md](docs/releasing.md#github-actions)**.

### Coverage (CI / local only)

```bash
mvn verify -Pcoverage
```

Aggregated JaCoCo report: `rose-coverage/target/site/jacoco-aggregate/index.html`

### GPG signing

**Do not** put `gpg.passphrase` in `settings.xml` — `maven-gpg-plugin` will warn and it is unsafe in disk/SCM.

| Mode | How |
|------|-----|
| **Local** | `gpg-agent` (default, `gpg.use.agent=true`). Run `gpg --sign` once or `export GPG_TTY=$(tty)` if the agent does not prompt. Optional: `-Dgpg.keyname=YOUR_KEY_ID` on the command line. |
| **CI / batch** | Prefer `gpg-agent` in the workflow, or pass `--pinentry-mode loopback` and the passphrase via CI secrets (do not commit passphrases to `settings.xml`). |

```bash
# Local release (gpg-agent prompts or uses cached passphrase)
mvn deploy -Prelease

# CI / batch (example; store passphrase in a secret, not settings.xml)
mvn deploy -Prelease -Dgpg.arg="--pinentry-mode loopback" -Dgpg.passphrase="$MAVEN_GPG_PASSPHRASE"
```

### Troubleshooting

| Symptom | Fix |
|---------|-----|
| `403 Forbidden` on `maven-snapshots` | Enable **SNAPSHOTs** for `io.zhijun` on Central Portal; verify `central` token; avoid `altSnapshotDeploymentRepository` / corporate Nexus profiles during deploy. |
| `403` on release version | Namespace not verified, wrong token, or GPG key not on keyserver. |
| Error on `rose-coverage` during deploy | Do not use `-Pcoverage` with `deploy`. Default reactor excludes it. |
| GPG passphrase warning | Remove `gpg.passphrase` from settings; use gpg-agent or `MAVEN_GPG_PASSPHRASE`. |
