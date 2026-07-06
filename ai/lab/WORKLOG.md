<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Work ledger — papyrus-web-plus-plus

The repo's **episodic memory of work**: one row per unit of work (feature added,
bug fixed, refactor, process change), linking the spec that authorized it, the
decisions behind it, the review that checked it, the evaluation that scored it,
and the commits that shipped it. The maps in `ai/guide/` say what the repo *is*;
this ledger says *what was done to it, when, and under which contract*.

## Rules

- **Append-only.** New work gets a new row with the next `W-<n>` ID. Never delete
  or renumber rows; a rolled-back change gets Status `rolled-back`, not removal.
- **One row per unit of work** — the same unit the spec describes. A row without
  a spec link is a process violation, not a shortcut.
- **Backtick every artifact path, written from the repo root** (e.g.
  ai/lab/specs/SPEC_x.md, backticked). `verify` checks backticked paths against
  the file tree, so a row whose artifacts vanished fails CI instead of rotting
  silently. Use `—` for artifacts that genuinely don't apply (e.g. no ADR was
  needed).
- **Agents append rows tagged `[inferred]`** like everything else in `ai/`;
  the human flips them to `[verified]` when auditing. Never flip it yourself.
- **Status vocabulary:** `specced` → `in-progress` → `in-review` → `shipped`
  (or `rolled-back` / `dropped`).
- **Type vocabulary:** `feature` · `bugfix` · `refactor` · `docs` · `process`.

## Ledger

<!-- Example row (copy, replace the angle-bracket fields, backtick real paths
     written from the repo root). The example ID W-000 is reserved for this
     comment — real rows start at W-001:
| W-000 | 2026-01-15 | feature | Short title | ai/lab/specs/SPEC_<name>.md | ai/lab/decisions/ADR_<n>-<t>.md | ai/lab/reviews/REVIEW_W-000.md | ai/lab/evaluations/EVAL_<name>.md | <commit/PR> | FEATURE_MAP row | shipped | [inferred] |
-->

| ID | Date | Type | Title | Spec | ADRs | Review | Eval | Commits / PR | Knowledge updated | Status | Provenance |
|---|---|---|---|---|---|---|---|---|---|---|---|
| W-001 | 2026-07-06 | process | Ignore Eclipse project files and hide from VS Code | — | — | — | — | — | `.gitignore`, `.vscode/settings.json` | shipped | [inferred] |
| W-002 | 2026-07-06 | process | Untrack Eclipse project, classpath, and settings files from Git repository | — | — | — | — | — | — | shipped | [inferred] |
| W-003 | 2026-07-06 | process | /cold-start bootstrap: drafted module map, guides, and diagrams for human audit | — | — | — | — | (uncommitted) | `ai/guide/MODULE_MAP.md`, `ai/guide/ARCHITECTURE.md`, `ai/guide/PROJECT_OVERVIEW.md`, `ai/guide/FEATURE_MAP.md`, `ai/guide/CONVENTIONS.md`, `ai/analysis/diagrams/package-deps.mmd`, `ai/analysis/diagrams/domain-core.mmd`, `ai/analysis/diagrams/seam.mmd` | in-review | [inferred] |
| W-004 | 2026-07-06 | process | Fill Stack/Build/Test placeholders in config files (maintainer-approved, per /review-agent-config findings C5–C7, A3, A10) | — | — | — | — | (uncommitted) | `CLAUDE.md`, `AGENTS.md` | shipped | [inferred] |
| W-005 | 2026-07-06 | process | Record maintainer verification of MODULE_MAP (flip to [verified] at maintainer's explicit direction); fix truncated description sentence | — | — | — | — | (uncommitted) | `ai/guide/MODULE_MAP.md`, `CLAUDE.md`, `AGENTS.md` | shipped | [inferred] |
| W-006 | 2026-07-06 | process | /post-cold-start-verification audit: 1 P1, 3 P2, 4 P3 findings | — | — | — | — | (uncommitted) | `ai/analysis/audit-reports/2026-07-06-post-cold-start.md` | shipped | [inferred] |
| W-007 | 2026-07-06 | process | Fix verify-gate wording: command runs from the kit checkout, not repo root (audit finding P1-1) | — | — | — | — | (uncommitted) | `CLAUDE.md`, `AGENTS.md` | shipped | [inferred] |
| W-008 | 2026-07-06 | process | Propagate verify/drift kit-checkout wording to README and the four check-drift mirrors | — | — | — | — | (uncommitted) | `README.md`, `.claude/commands/check-drift.md`, `.cursor/rules/check-drift.mdc`, `.agents/workflows/check-drift.md`, `.github/prompts/check-drift.prompt.md` | shipped | [inferred] |


