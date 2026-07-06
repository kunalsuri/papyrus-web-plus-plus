<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Post-cold-start verification — 2026-07-06  `[inferred]`

> Audit of the AI knowledge layer after the /cold-start pass, the maintainer's
> MODULE_MAP audit, and the config placeholder fills (worklog `W-003`–`W-005`).
> Context: commit `6e9b3cf2` + uncommitted working-tree changes.
> Method: kit manifest review (`ai/analysis/audit-reports/VERIFICATION_MANIFEST.json`),
> placeholder/provenance greps across `ai/` + `CLAUDE.md` + `AGENTS.md`, and the
> 22-path existence spot check performed at draft time (all OK).
> This report is `[inferred]` — a human should confirm each finding before acting.

## Check summary

| # | Check | Result |
|---|---|---|
| 1 | Placeholders (`<fill in>`, `?` stability, `{{…}}`) | 1 file with leftovers (`ai/repo-profile.json`); 2 intentional `?` rows; entry files clean |
| 2 | Internal consistency (paths, rows, diagrams) | Clean per spot check; mechanical manifest is stale → regenerate (P2-3) |
| 3 | Profile consistency (commands vs `ai/repo-profile.json`) | DIVERGED — profile still has `<fill in>` commands (P2-2) |
| 4 | Provenance hygiene (`[verified]` tags) | Clean — only 3 dated tags, human sign-off documented (see notes) |

## P1 — agent-blocking

1. **Verify-gate command cannot run from this repo.** `CLAUDE.md` (hard rule
   "Verify claims") and `AGENTS.md` (rule 9) instruct: `node install.mjs verify . --strict`.
   No `install.mjs` exists anywhere in this repo (confirmed by glob, twice); the kit's
   verify script lives in the **ai-fication-kit checkout** and is invoked from there as
   `node install.mjs verify <path-to-this-repo> --strict`. Every agent that follows the
   rule literally will fail its definition-of-done.
   *Fix:* reword both rules to name the kit-checkout invocation (or vendor/alias a
   verify script into this repo, e.g. a `scripts-pwpp/verify.ps1` wrapper).

## P2 — misleading

2. **`ai/repo-profile.json` diverges from the entry files.** Lines 9–10 still say
   `"buildCmd": "<fill in>"` / `"testCmd": "<fill in>"`, `languages`/`buildSystems`
   are empty arrays, and `description` carries the truncated sentence that was fixed
   in `CLAUDE.md`/`AGENTS.md` today. The profile is deterministic `orient` output —
   flagging, not fixing.
   *Fix:* re-run the kit's `orient` step with explicit `--build`/`--test` values (the
   profile's own `notes` field says to set them manually), or have a human edit the file.

3. **`ai/analysis/audit-reports/VERIFICATION_MANIFEST.json` is stale.** Generated
   2026-07-06T14:28:32Z — before /cold-start populated the guides, before the diagrams
   existed, and before the `CLAUDE.md`/`AGENTS.md` edits. Its 40/40-confirmed summary
   describes the *scaffolded* docs, not today's content.
   *Fix:* re-run kit `verify` against this repo to regenerate the manifest and
   `VERIFICATION_REPORT.md`. Interim mitigation: the 22 load-bearing paths written
   during cold-start were manually existence-checked at draft time (all present).

4. **`ai/analysis/FEATURE_CATALOG.md` (+ `_BACKEND`/`_FRONTEND`) are still templates**
   (example feature "F1") while being referenced from `ai/guide/FEATURE_MAP.md` and
   `ai/INDEX.md`. Mitigated: FEATURE_MAP's header explicitly says the catalog is a
   placeholder.
   *Fix:* run `/create-feature-catalog` when feature work is planned; until then the
   existing caveat suffices.

## P3 — cosmetic

5. **Two intentional `?` Stability rows** in `ai/guide/MODULE_MAP.md`:
   `backend/papyrus-web-resources/` (on disk, absent from `backend/pom.xml`
   `<modules>`) and `.github/` (mixed upstream/fork ownership). Both are flagged
   in-row; the safe default (`?` ⇒ treat as `frozen`) covers agents.
   *Fix:* resolve when convenient; no agent action needed.

6. **`[verified] required` template idiom** (`ai/guide/ARCHITECTURE.md` "Invariants"
   heading, `ai/guide/CONVENTIONS.md` "Things that look wrong but are right" heading)
   can be misparsed by naive grep tooling as an undated `[verified]` tag.
   *Fix:* consider rewording to "(human-only section — entries must be
   `[verified] (date)`)".

7. **Mixed provenance vocabulary** in `ai/guide/PROJECT_OVERVIEW.md` Stack heading:
   `[inferred — verified against real config files 2026-07-06]`. Honest, but tag
   scanners count tokens, not nuance.
   *Fix:* human either flips it to a clean `[verified] (date)` during audit or the
   qualifier moves into prose under a plain `[inferred]` tag.

8. **Historical snapshot fields in `ai/repo-profile.json`** (`existingAIConfig.claudeMd.exists:
   false`, etc.) were true at generation time, false now. Harmless point-in-time record.
   *Fix:* cleared automatically if `orient` is re-run per finding 2.

## Provenance notes (check 4 detail)

- The only `[verified]` tags in `ai/` are the three section tags in
  `ai/guide/MODULE_MAP.md`, all dated `(2026-07-06)`. They were flipped at the
  maintainer's explicit direction after their read-through; the sign-off trail is
  recorded in the MODULE_MAP header and worklog row `W-005`.
- All agent-written rows and drafts carry `[inferred]`. No undated `[verified]`
  outside the template idiom in finding 6. No `{{…}}` or `TODO` leftovers anywhere
  in `ai/`. `ai/lab/` template files contain angle-bracket placeholders by design
  (they are templates) and are excluded from finding 1.

## Not audited

Content *quality* of `ai/guide/` prose beyond consistency (out of scope per the
skill), and `ai/START-HERE.html` (installer-owned, refreshed by kit commands).
