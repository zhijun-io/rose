# Rose Dev Services

Testcontainers-backed local infrastructure for Rose dev and test profiles.

| Module | Artifact | Role |
|--------|----------|------|
| API | `rose-dev-services-api` | Shared configuration contracts |
| Core | `rose-dev-services-core` | Registration and bootstrap integration |
| Connectors | `rose-dev-services-{technology}` | Optional runtime dependencies per technology |
| Tests | `rose-dev-services-tests` | Shared integration-test support (test scope) |

Connectors are optional `runtime` dependencies — there is no per-connector starter.
