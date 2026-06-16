# Releasing Rose to Maven Central

**Audience:** Maintainers publishing `io.zhijun` artifacts.  
**Related:** [CONTRIBUTING.md](../CONTRIBUTING.md#releasing-to-maven-central-sonatype), [development-principles.md](development-principles.md).

Rose uses `${revision}` in the root `pom.xml` and publishes via the [Central Publisher Portal](https://central.sonatype.org/publish/publish-portal-maven/) with the `release` Maven profile (`sources`, `javadoc`, GPG, `central-publishing-maven-plugin`).

---

## Prerequisites

Complete these once (and re-check before each release).

| Item | Requirement |
|------|-------------|
| Central namespace | [`io.zhijun`](https://central.sonatype.com/publishing/namespaces) registered and **verified** |
| Portal token | [Generate a user token](https://central.sonatype.org/publish/generate-portal-token/); add to `~/.m2/settings.xml` with server id **`central`** |
| GPG | Signing key available locally; **public** key on a public keyserver ([requirements](https://central.sonatype.org/publish/requirements/#sign-files-with-gpgpgp)) |
| Quality gate | `mvn verify -Pcoverage` passes (CI runs the same) |
| Corporate Maven profiles | If `settings.xml` redirects deploy to an internal Nexus (e.g. `company`), disable that profile during Central deploy (`-P!your-corporate-profile`) |

**Do not** commit tokens or `gpg.passphrase` to the repository or `settings.xml`.

### `settings.xml` structure (example)

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username><!-- Portal token username --></username>
      <password><!-- Portal token password --></password>
    </server>
  </servers>
</settings>
```

### SNAPSHOT vs release

| Version | Example | Portal |
|---------|---------|--------|
| Snapshot | `0.1.0-SNAPSHOT` | Enable **SNAPSHOTs** for `io.zhijun` on Central Portal |
| Release | `0.1.0` (no suffix) | Verified namespace + GPG; no SNAPSHOT flag needed |

This document focuses on **release** (正式版) publishing. For automation, see [GitHub Actions](#github-actions) below.

---

## GitHub Actions

Rose uses reusable workflows from [zhijun-io/workflows](https://github.com/zhijun-io/workflows).

| Workflow | File | Trigger |
|----------|------|---------|
| Snapshot publish | `.github/workflows/maven-snapshot.yml` | Push to `main` when `<revision>` ends with `-SNAPSHOT`; or `workflow_dispatch` |
| Release | `.github/workflows/maven-release.yml` | `workflow_dispatch` (input: version, e.g. `0.1.0`) |

Configure these **repository or organization secrets** before the first publish:

| Secret | Used by |
|--------|---------|
| `MAVEN_USERNAME` | Snapshot + release (Central Portal token username) |
| `MAVEN_PASSWORD` | Snapshot + release (Portal token password) |
| `MAVEN_GPG_PRIVATE_KEY` | Snapshot + release (ASCII-armored private key) |
| `MAVEN_GPG_PASSPHRASE` | Snapshot + release |

**Snapshot:** push to `main` when `<revision>` ends with `-SNAPSHOT` runs `deploy -Prelease` only (`verify-first: false`; skipped when not a SNAPSHOT). Quality gate: **Maven CI** runs `mvn clean verify -Pcoverage` on the same commit — keep Maven CI as a required check on `main`.

**Release:** checks out `main`, sets `<revision>` to the input version and commits, runs `clean verify -Prelease`, deploys, pushes the release commit, creates tag `vX.Y.Z` and GitHub Release, then bumps `<revision>` to the next `-SNAPSHOT` on `main`.

CI (`.github/workflows/maven-ci.yml`) uses shared [zhijun-io/workflows](https://github.com/zhijun-io/workflows) `maven-ci.yml` — `mvn clean verify -Pcoverage`.

---

## Release workflow (example `0.1.0`)

### Option A — GitHub Actions (recommended)

1. Ensure [secrets](#github-actions) are configured.
2. Actions → **Maven Central Release** → Run workflow → enter `0.1.0`.
3. Confirm deployment on [Central Portal](https://central.sonatype.com/).

The workflow commits the release version, verifies and deploys with `-Prelease`, pushes the release commit, tags `v0.1.0`, creates a GitHub Release, and bumps `<revision>` to `0.2.0-SNAPSHOT` on `main`.

### Option B — Manual release

#### 1. Set the release version

In the root `pom.xml`, remove `-SNAPSHOT`:

```xml
<revision>0.1.0</revision>
```

All modules inherit `${revision}`. `flatten-maven-plugin` resolves the version on build.

#### 2. Commit and tag

```bash
git add pom.xml
git commit -m "chore: prepare release 0.1.0"
git tag v0.1.0
git push origin main
git push origin v0.1.0
```

#### 3. Verify (recommended)

```bash
mvn -B clean verify -Pcoverage -P!company
```

Replace `company` with your corporate profile id if applicable. **Do not** combine `-Pcoverage` with `deploy` (`rose-coverage` is not in the default reactor).

#### 4. Deploy to Maven Central

```bash
mvn -B clean deploy -Prelease -P!company
```

The `release` profile attaches:

- **sources** and **javadoc** JARs
- **GPG** signatures (`--pinentry-mode loopback`; passphrase from `MAVEN_MAVEN_GPG_PASSPHRASE` env)
- upload via **central-publishing-maven-plugin** (`autoPublish=true`)

#### GPG (loopback)

`release` profile configures `maven-gpg-plugin` with `--pinentry-mode loopback`. Export the passphrase before deploy:

```bash
export MAVEN_MAVEN_GPG_PASSPHRASE='…'   # do not commit; do not put in settings.xml
mvn -B clean deploy -Prelease -P!company
```

#### 5. Confirm on Central Portal

Open [Central Portal → Deployments](https://central.sonatype.com/) and confirm `io.zhijun:rose-*:0.1.0` artifacts published successfully.

#### 6. Bump to the next snapshot

On `main`, set the next development version:

```xml
<revision>0.2.0-SNAPSHOT</revision>
```

```bash
git commit -am "chore: bump to 0.2.0-SNAPSHOT"
git push origin main
```

#### 7. GitHub Release (optional)

```bash
gh release create v0.1.0 --title "0.1.0" --notes "Release notes here."
```

---

## After release: consumer usage

Applications **import** `rose-bom`; they **do not** inherit `rose-parent`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.zhijun</groupId>
            <artifactId>rose-bom</artifactId>
            <version>0.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

See [README.md](../README.md#quick-start) for starter dependencies.

---

## Snapshot deploy (reference)

With `<revision>…-SNAPSHOT</revision>` and SNAPSHOTs enabled on the namespace:

**GitHub Actions:** push to `main` (or run **Maven Publish Snapshot** manually) when `<revision>` is a SNAPSHOT — `deploy -Prelease` only; CI covers `verify -Pcoverage`. See [GitHub Actions](#github-actions).

**Local:**

```bash
mvn -B clean deploy -Prelease -P!company
```

Use snapshots to test the publish pipeline before the first release.

---

## Troubleshooting

| Symptom | Likely fix |
|---------|------------|
| `403 Forbidden` on `maven-snapshots` | Enable SNAPSHOTs for `io.zhijun`; verify `central` token; use `-P!corporate-profile` |
| `403` on release version | Namespace not verified, wrong token, or GPG public key not on keyserver |
| GPG / signing errors | Export `MAVEN_MAVEN_GPG_PASSPHRASE`; confirm secret key with `gpg --list-secret-keys`; public key on keyserver |
| `rose-coverage` errors during deploy | Do not pass `-Pcoverage` with `deploy` |
| GPG passphrase warning in Maven | Remove `gpg.passphrase` from `settings.xml` |

---

## Release checklist

- [ ] `mvn verify -Pcoverage` green (or CI **Maven CI** passed)
- [ ] Central secrets configured (`MAVEN_*`, `GPG_*`) if using GitHub Actions
- [ ] **Option A:** run **Maven Central Release** with version `X.Y.Z`, or **Option B:** manual steps below
- [ ] `<revision>` set to release version (no `-SNAPSHOT`) — manual only; workflow sets this automatically
- [ ] Commit + annotated tag `vX.Y.Z` pushed — manual only
- [ ] `mvn clean deploy -Prelease` succeeded — manual only
- [ ] Central Portal shows published artifacts
- [ ] `<revision>` bumped to next `-SNAPSHOT` on `main`
- [ ] (Optional) GitHub Release created
- [ ] (Optional) README / changelog updated for user-visible changes
