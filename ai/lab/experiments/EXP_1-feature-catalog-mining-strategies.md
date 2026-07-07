<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# EXP 1: Feature-catalog mining strategies (subagents vs inline scans)
> **Date:** 2026-07-07 · **Status:** concluded
> Provenance: `[inferred]` — written by the agent that ran the trial; audit before citing.

## Question
What is the most reliable agent strategy for deep-mining this repo's feature catalog
(`/create-feature-catalog`) under real-world constraints (plan session-usage limits),
and how do we make "complete surface" claims trustworthy?

## Setup
- Agent: Claude Code (model Fable 5), workflow `.claude/commands/create-feature-catalog.md`
- Repo @ commit `457599aa`; output shipped as commit `32cd68f9` (WORKLOG `W-009`)
- Two mining approaches trialed in the same session, in sequence.

## Observations
1. **Approach A — 3 parallel `repo-explorer` subagents** (frontend / backend / tests),
   report-only-at-end design. All three ran ~5.5 min (21–35 tool calls each), then hit
   the plan's session usage limit BEFORE emitting their final reports. Recoverable
   output: **zero**. The all-or-nothing final-message pattern means a cutoff loses the
   entire exploration.
2. **Approach B — inline deterministic mining** in the main session: ~25 targeted
   Grep/Glob/PowerShell calls in 4 batches. Every intermediate result landed durably
   in-context; a second cutoff would have lost at most one batch. What made it cheap:
   features cluster around mechanical markers — `@QueryDataFetcher`/`@MutationDataFetcher`
   annotations, `*DiagramDescriptionBuilder` classes, Cypress spec filenames,
   package-tree listings with file counts.
3. **Completeness near-miss (the key lesson).** The first API scan was scoped to
   `backend/papyrus-web-application/` and claimed "11 GraphQL operations — complete".
   A repo-wide rescan (triggered by the maintainer's confidence question) found **19
   more operations** in `backend/papyrus-web-custom-widgets/` plus 7 `.graphqls`
   schema files. Fixed before commit. A completeness claim is only as good as the
   scope of the scan that backs it.
4. **Verification without the kit** (ai-fication-kit not installed in-session):
   regex-extract backtick-quoted claims from changed docs; existence-check absolute
   paths (320 checked); resolve relative subpackage names against their stated parent
   dirs (28 checked). Two URL routes had to be de-backticked because they look like
   absolute paths to a checker.
5. **Calibration delivered to the human:** ~95% for mechanically verified facts,
   ~85% for interpretive clustering/naming, 50–70% for the 5 explicitly-UNSURE items
   listed in `ai/analysis/FEATURE_CATALOG.md` §6.

## Conclusion
- **Adopt:** inline marker-based mining as the default for catalog-style deep scans
  when budget is uncertain; reserve subagent fan-out for runs whose loss is cheap
  (or until subagents can checkpoint partial reports).
- **Adopt:** every "complete <surface>" claim must be backed by a **repo-wide** scan
  of the marker (all `backend/*/src/main/java`, not the expected package), and the
  catalog should state the scan scope next to the claim (done in
  `ai/analysis/FEATURE_CATALOG.md` §2).
- **Adopt:** mechanical backtick-path existence checks before committing `ai/` docs
  whenever the kit's `verify` is unavailable.
- **Candidate for the human (config-churn rule — agent must not do this):** fold the
  two "Adopt" items above into `.claude/commands/create-feature-catalog.md` so future
  runs inherit them.
- **Dropped:** nothing — the subagent approach stays valid under roomier budgets.
