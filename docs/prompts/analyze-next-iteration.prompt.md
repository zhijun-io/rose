---
agent: 'agent'
description: 'Analyze Rose next iteration direction by benchmarking microsphere-projects and current repo state'
---

## Role

You are a senior platform architect planning the **next iteration** for the Rose repository (`io.zhijun`, Spring Boot 2.7 / Java 8). You produce an **actionable roadmap** grounded in evidence—not a feature wishlist copied from another org.

## Inputs

| Variable | Description | Default |
|----------|-------------|---------|
| `${input:org_url:GitHub org to benchmark}` | Organization repos page | `https://github.com/orgs/microsphere-projects/repositories` |
| `${input:horizon:Planning horizon}` | e.g. `Phase 0–2`, `next quarter` | `Phase 0–2 (8 weeks)` |
| `${input:focus:Extra emphasis}` | Optional theme, e.g. `rose-spring-boot`, `i18n` | _(empty)_ |
| `${input:output_path:Where to save roadmap MD}` | Path relative to repo root | `docs/design/rose-next-iteration-roadmap.md` |

## Guardrails

1. **Read before claiming**
   - Rose: root `README.md`, `docs/design/README.md`, `docs/rose-conventions.md`, existing `docs/design/microsphere-benchmark-notes.md` if present
   - Microsphere: use `gh api orgs/<org>/repos` and READMEs of **relevant** repos only (spring, spring-boot, mybatis, i18n, test, observability, bom, build)
2. **Grep before claiming missing** — a capability in another Rose module is a **documentation gap**, not absence
3. **Do not copy Microsphere feature-by-feature** — map to Rose **existing themes** or **approved design specs** only
4. **Respect Rose non-goals:** Spring Cloud, WebFlux, Guice, gateway/resilience stacks unless `${input:focus}` explicitly requests
5. **Java 8 / Boot 2.7** is the baseline unless documenting a **future branch strategy**
6. Cite **`file:line` or design doc §** for Rose claims; cite **repo URL + README section** for external claims
7. Separate **pre-existing gaps** from **work introduced in current diff** when reviewing uncommitted changes

## Workflow

### Step 1 — Recon (parallel)

- List Microsphere org repos: name, description, language, `updated_at`, stars
- Cluster into layers: build/BOM → java → spring → boot → data → observability → runtime/ecosystem
- Snapshot Rose reactor: root `pom.xml` modules, `docs/design/*` implementation status tables (✅/🟡/❌)

### Step 2 — Overlap matrix

Build a table:

| Capability | Rose module | Status | Microsphere analogue | Gap (1 line) |

Mark Rose **leading**, **aligned**, **planned (design exists)**, **not applicable**.

### Step 3 — Strategic judgment

Answer in prose (short):

- Where Rose should **differentiate** (keep investing)
- Where Rose should **align platform structure** (not feature parity)
- **Explicit non-goals** for `${input:horizon}`

### Step 4 — Phased roadmap

Produce **3–4 phases** with:

- Phase name + time hint
- Numbered work items (verb-led)
- Link to Rose design doc or Microsphere reference
- **Acceptance command** (`./mvnw …`) where applicable
- Breaking-change risk: Low / Medium / High

Priority order:

1. Platform stability & tests (Phase 0)
2. Boot platform layer gaps with existing design (Phase 1)
3. New themes with **approved specs** only (Phase 2)
4. Long-term / branch strategy (Phase 3+)

### Step 5 — Deliverables

1. Write or update **`${input:output_path}`** with sections:
   - 对标快照
   - Rose 能力矩阵
   - 战略判断
   - 分阶段路线
   - 模块速查
   - 成功指标
   - 修订记录
2. Update `docs/design/README.md` index row if new doc
3. Optionally append a **one-paragraph summary** for CHANGELOG/wik i (do not edit CHANGELOG unless user asks)

## Output format (chat summary)

After saving the file, reply with:

**📍 结论（3 bullets）** — top priorities for `${input:horizon}`

**📄 文档** — link to `${input:output_path}`

**⚠️ 风险** — top 2 breaking-change or scope risks

**⏭ 建议下一步** — single concrete task to start tomorrow

## Quality checklist

- [ ] Every Phase 0 item is verifiable locally (mvn command or doc path)
- [ ] No item lacks a Rose module or design doc anchor
- [ ] Non-goals section exists and names at least 3 excluded ecosystems
- [ ] Status matrix uses ✅/🟡/❌ consistently
- [ ] Roadmap references `rose-bom` / consumer impact where artifacts change
- [ ] Chinese prose for maintainer-facing doc; technical identifiers in English

## Example invocation

```
使用 docs/prompts/analyze-next-iteration.prompt.md，
对标 https://github.com/orgs/microsphere-projects/repositories，
规划 Phase 0–2，重点 rose-spring-boot 与 rose-i18n，
输出到 docs/design/rose-next-iteration-roadmap.md
```

## Related prompts / skills

- Unit tests: `sdd-skills/.github/prompts/generate-unit-tests.prompt.md`
- Code review: `sdd-skills/.github/prompts/review-code.prompt.md`
- Delivery gate review: `sdd-review` skill (scoped increment—not this prompt)
