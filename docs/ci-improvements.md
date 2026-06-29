# CI Improvements

This one-pager collects the most practical CI ideas borrowed from `temporalio/sdk-java`, trimmed for Rose's current
baseline.

## Keep the current baseline

- Keep `validate` as the fast gate.
- Keep full `verify` for Docker-backed `*IT` and coverage.
- Keep CI focused on Maven only; do not add extra build systems.

## High-value improvements

1. Add a dedicated compatibility job for the highest-risk runtime or dependency axis instead of mixing it into every
   unit job.
2. Split quality gates so failures are easier to read: build, tests, and coverage should fail independently.
3. Keep release publishing separate from CI validation.
4. Add path filters so docs-only changes avoid heavyweight jobs when possible.
5. Keep local parity commands documented right next to the CI matrix.

## Suggested order

| Priority | Change | Why |
| --- | --- | --- |
| P1 | Compatibility job | Catches breakage earlier |
| P1 | Split gates | Faster triage |
| P2 | Path filters | Saves runner time |
| P2 | Release split | Reduces accidental coupling |

## Rule of thumb

Borrow the shape of `sdk-java`, not its whole workflow. The goal is faster feedback with less CI noise, not a bigger
pipeline.
