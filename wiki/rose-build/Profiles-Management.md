# Profiles Management

Maven profiles provided by `rose-build`. Inherited by all reactor modules.

---

## Manual Profiles

Activate with `-P<profile>` (comma-separated for multiple).

| Profile    | Purpose                                               | Typical command                 |
|------------|-------------------------------------------------------|---------------------------------|
| `coverage` | JaCoCo agent + per-module `target/site/jacoco/`       | `mvn verify -Pcoverage`         |
| `docs`     | AsciiDoc / DocBook generation (root + example module) | `mvn generate-resources -Pdocs` |
| `release`  | Javadoc, GPG sign, Central Portal publish             | `mvn deploy -Prelease`          |

### `coverage`

- **Not** active by default; CI runs `verify -Pcoverage`.
- POM-only modules may set `<jacoco.skip>true</jacoco.skip>`.
- Reports: `<module>/target/site/jacoco/index.html`

### `docs`

- Root `docs` profile configures Asciidoctor at parent level (`inherited=false`).

### `release`

Requires GPG key and Sonatype Central Portal credentials. See [CI-CD-Integration](CI-CD-Integration).

---

## JDK-Activated Profiles

These activate automatically based on the JDK running Maven.

| Profile     | JDK range | Effect                                             |
|-------------|-----------|----------------------------------------------------|
| `java8+`    | ≥ 8       | Javadoc `-Xdoclint:none`                           |
| `java9+`    | ≥ 9       | Sets `maven.compiler.release`                      |
| `java9-15`  | 9–15      | Surefire `--illegal-access=permit`                 |
| `java16-20` | 16–20     | Surefire `--add-opens` for Mockito/reflection      |
| `java21+`   | ≥ 21      | Same `--add-opens` + Byte Buddy experimental flags |

---

## Test Lifecycle

| Plugin   | Phase                        | Includes                           | Command      |
|----------|------------------------------|------------------------------------|--------------|
| Surefire | `test`                       | `*Test`, `*Tests` (excludes `*IT`) | `mvn test`   |
| Failsafe | `integration-test`, `verify` | `*IT`                              | `mvn verify` |

Skip integration tests: `mvn verify -DskipITs`

---

## Comparison with Microsphere Build

Rose `rose-build` follows the same profile layout
as [microsphere-build](https://github.com/microsphere-projects/microsphere-build/wiki/Profiles-Management):

| Microsphere  | Rose                      | Notes                                                          |
|--------------|---------------------------|----------------------------------------------------------------|
| `publish`    | `release`                 | Central Portal publishing                                      |
| `test`       | default surefire/failsafe | Rose binds test plugins in parent (no separate `test` profile) |
| `coverage`   | `coverage`                | Same                                                           |
| `docs`       | `docs`                    | Same                                                           |
| JDK profiles | JDK profiles              | Same naming convention                                         |
