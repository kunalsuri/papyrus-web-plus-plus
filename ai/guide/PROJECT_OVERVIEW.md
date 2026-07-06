<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Project overview — papyrus-web-plus-plus

> Status: drafted by /cold-start on 2026-07-06 (first run: 2026-07-06 per
> `ai/repo-profile.json` → `humanContext`); every section `[inferred]` until audited.

## What this is
An experimental sandbox for turning a complex, industrial-grade codebase into an
AI-native one. This is a FORK of **papyrus/org.eclipse.papyrus-web** (upstream:
`https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web.git`).
Papyrus Web is a web-based UML modeling tool built on Sirius Web / Sirius Components.

## Stack  `[inferred — verified against real config files 2026-07-06]`
- Languages: Java 21 (backend, ~1000 files), TypeScript 5.4 / React 18.3 (frontend), PowerShell (`scripts-pwpp/`)
- Frameworks: Spring Boot (parent 4.0.5) + Sirius Components 2026.5.0 (backend); Vite 8 + turbo workspaces, Node 22.16.0 / npm 10.9.2 pinned (frontend); PostgreSQL 15
- Build (helper, ours): `.\scripts-pwpp\build-all.ps1` (add `-WithTests` / `-FixFormat`)
- Build (manual): `npm ci && npm run build` in `frontend/`, copy `frontend/papyrus-web/dist/*` → `backend/papyrus-web-frontend/src/main/resources/static`, then `mvn clean verify -DskipTests` in `backend/` → fat JAR `backend/papyrus-web/target/papyrus-web-<VERSION>.jar`
- Test: `mvn clean verify` in `backend/` (default suite; `-DallTests` ≈ 7 h per upstream README); `npm run test` in `frontend/papyrus-web/` (vitest); Cypress in `integration-tests/` against `http://localhost:8080`
- Run (dev): `.\scripts-pwpp\start-dev.ps1` — DB `:5439` → backend `:8080` → Vite `:5173`

## Why it exists  `[inferred]`
The fork exists to experiment with making a large, industrial Eclipse codebase legible
and safely workable for AI coding agents: a committed, tool-agnostic knowledge layer
(`ai/`), agent configs for multiple tools, and guarded workflows over a frozen upstream.
The upstream product itself gives engineers a web-based Papyrus UML modeler (diagrams,
profiles, properties editing) without an Eclipse desktop install.

## What we add vs. what we inherit  `[inferred]`
OBSERVED via `git diff upstream-master...main` (2026-07-06): the fork has changed
**no upstream Java/TS source**. Inherited (frozen): `backend/`, `frontend/`,
`integration-tests/`, `doc/`, `scripts/`, `dockerfiles/`. Ours: `ai/`, `.claude/`,
`.agents/`, `.cursor/`, most of `.github/` (chatmodes, prompts, `ai-check.yml`),
`scripts-pwpp/`, `doc-pwpp/`, `CLAUDE.md`, `AGENTS.md`, `SECURITY.md`, `README.md`
(fork sections), `papyrus.code-workspace`, plus deletions of committed Eclipse IDE
metadata (`.project`/`.classpath`/`.settings`). Sync strategy: `upstream-master` is a
pristine GitLab mirror; `main` carries our delta (see `doc-pwpp/FORK-SETUP.md`).

## Glossary  `[inferred]`
| Term | Meaning here |
|---|---|
| Papyrus | Eclipse's UML modeling platform; this repo is its web variant |
| Sirius Web / Sirius Components | Obeo's framework for web modeling tools; supplies the app shell, GraphQL API, and diagram/form/tree renderers |
| UML profile | Extension mechanism for UML models (stereotypes); first-class feature here (`profile` bounded context, bundled C++/Java/codegen/transformation profiles) |
| Representation | A Sirius view of a model (diagram, table, tree, form) |
| Data fetcher | Sirius Components' GraphQL resolver pattern (`IDataFetcher`) used by backend controllers |
| pwpp | "papyrus-web-plus-plus" — suffix marking OUR fork-only directories (`scripts-pwpp/`, `doc-pwpp/`) |
| upstream-master | Local branch mirroring GitLab upstream exactly; never edited directly |
