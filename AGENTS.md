<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# AGENTS.md — papyrus-web-plus-plus

Tool-agnostic instructions for any AI coding agent (Claude Code, Codex, Cursor,
Copilot, Windsurf). Claude Code reads these via `@AGENTS.md` in `CLAUDE.md` (see [CLAUDE.md](CLAUDE.md));
other tools read this file directly.

## Project
Papyrus web is a web UML modeler to easily create UML diagrams directly from a web browser. It is based on Eclipse Sirius Components (https://www.eclipse.org... This is a FORK of **papyrus/org.eclipse.papyrus-web** (upstream).
Stack: Java 21 & Spring Boot on Eclipse Sirius Components (backend) · TypeScript, React 18, Vite 8, MUI 7 & Turbo (frontend) · PostgreSQL 15.

## Rules every agent must follow
1. **Frozen upstream.** Code inherited from **papyrus/org.eclipse.papyrus-web** is off-limits unless the task explicitly requires it. New work goes in our own modules.
2. **No layout churn.** Don't reorganize directories; existing contributors depend on
   the structure.
3. **Test before done.** Run the suite matching your change; never declare success
   untested. Build: `powershell -File .\scripts-pwpp\build-all.ps1` · Test: `powershell -File .\scripts-pwpp\build-all.ps1 -WithTests`
4. **Match license headers** on every new source file, copying from neighboring files.
5. **Locate, then read.** Use `ai/guide/MODULE_MAP.md` to find code; grep before
   reading whole files.
6. **Surgical diffs.** Change only what the task needs.
7. **Provenance.** Anything you write into `ai/` is `[inferred]` until a human flips
   it to `[verified]`. Never flip that tag yourself.
8. **No Phantom Bugs & Configuration Churn.** Do not rewrite, restructure, or simplify configuration or instruction files (`CLAUDE.md`, `AGENTS.md`, `package.json`, or `ai/guide/` documents) based on quick searches or automated suggestions. Keep edits surgical. Never replace detailed guides with simplified stubs.
9. **Verify claims.** Before declaring a task finished, run `node install.mjs verify . --strict` (or equivalent test runner validation script) to ensure no file paths in the knowledge documents are broken.

## Knowledge map
**Navigation (fast path):** `ai/guide/MODULE_MAP.md` (where code lives),
`ai/guide/ARCHITECTURE.md` (system shape), `ai/guide/FEATURE_MAP.md` (feature → files
+ gotchas), `ai/guide/CONVENTIONS.md` (how to write code here).

**Generated analysis (on demand):** `ai/analysis/` — feature catalog, audit reports,
diagrams. **Development intelligence:** `ai/lab/` — specs, ADRs, evaluations.

**Machine-readable facts:** `ai/repo-profile.json` (produced deterministically by the
kit's `orient` step — trust it for stack facts, verify before relying on commands).

## Repo intelligence (the `ai/` knowledge-base)
The `ai/` folder is the single source of truth for repository intelligence. It is
tool-agnostic: any AI coding agent (Claude, Cursor, Copilot, Codex) reads the same
verified maps, architecture docs, and feature catalogs. New features, refactors, and
onboarding all start here.

If backup files (e.g. `CLAUDE_bkp_*.md` / `AGENTS_bkp_*.md`) exist, they are the prior
configuration. Knowledge was extracted from them during /cold-start — use `ai/guide/`
as the authoritative source instead.

<!-- Installed by ai-fication-kit 0.1.0 on 2026-06-29. -->
