<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Project overview — papyrus-web-plus-plus

> Status: drafted by `/cold-start` on 2026-06-29 (kit first-run 2026-06-29); every
> section `[inferred]` until a human audits it. Stack facts below were **verified**
> against `README.adoc`, `pom.xml`, and `package.json` during the pass.

## What this is
Papyrus Web is a **web-based UML modeler** — create UML diagrams directly from a
browser. It is built on **Eclipse Sirius Components / Sirius Web**
(https://www.eclipse.org/sirius/sirius-web.html). This repository is a **fork of
`papyrus/org.eclipse.papyrus-web`** (upstream lives on GitLab; this fork on GitHub).
It is a **mono-repo** holding both the frontend and the backend.

## Stack (verified 2026-06-29 against config files)
- **Backend:** Java **21** (Temurin), **Maven 3.9.9** multi-module reactor (21 modules),
  Spring Boot server on Eclipse Sirius Components. Persists to **PostgreSQL 15**.
- **Frontend:** **TypeScript**, **Vite 8**, **React 18**, **MUI 7**, `@xyflow/react`
  diagram canvas, **Apollo GraphQL** client. Monorepo via **npm workspaces + turbo 2.4.4**.
  Requires **Node ≥ 22.16.0**, **npm ≥ 10.9.2**.
- **API / seam:** GraphQL over **HTTP** (queries/mutations) + **WebSocket** subscriptions
  (`subscriptions-transport-ws`) — the standard Sirius Components transport.
- **Packaging:** the frontend is built, then copied into
  `backend/papyrus-web-frontend/src/main/resources/static/` and served by the backend as
  a **single fat JAR** on **:8080** (see `ARCHITECTURE.md`).

### Build / Test (verified — `README.adoc` §Building + `scripts-pwpp/build-all.ps1`)
- **Frontend** (from `frontend/`): `npm ci` → `npm run build` (turbo: components lib → app → `papyrus-web/dist`).
- **Bundle:** copy `frontend/papyrus-web/dist/*` → `backend/papyrus-web-frontend/src/main/resources/static`.
- **Backend** (from `backend/`): `mvn clean verify` (runs tests; **needs Docker**) · `mvn clean verify -DskipTests` to skip.
- **All-in-one (ours):** `scripts-pwpp/build-all.ps1` (`-WithTests`, `-FixFormat`, `-FrontendOnly`, `-BackendOnly`).
- **Frontend unit tests:** `npm test` (vitest) inside `frontend/papyrus-web`. **E2E:** Cypress in `integration-tests/`. `[inferred]`
- ⚠ **Build prerequisite:** a **GitHub Access Token** (`read:package`) must be configured
  in `~/.m2/settings.xml` *and* `~/.npmrc` to pull `sirius-emf-json` / `sirius-components`
  from GitHub Packages. Without it the build cannot resolve dependencies.

## Why it exists  `[inferred]`
Upstream Papyrus Web brings the desktop Eclipse Papyrus UML modeling experience to the
browser. This **fork** (`papyrus-web-plus-plus`, "pwpp") tracks upstream closely
(pristine-mirror sync) while adding fork-specific tooling and incremental features on
top — the goal appears to be a smoother local build/onboarding experience and
fork-owned extensions, without diverging from upstream's module structure. *A human
should confirm the fork's actual product intent.*

## What we add vs. what we inherit  `[inferred]`
- **Inherited (upstream — treat as `frozen`):** everything under `backend/`, `frontend/`,
  `integration-tests/`, `doc/`, `scripts/`. All modules use upstream `papyrus-web-*` /
  `@eclipse-papyrus/*` names. There is **no `-pwpp` suffix inside the source trees**, so
  fork edits (if any) are **interleaved in-place** — identify them via `git log`/`git blame`
  on commits **not** prefixed `GL-###` / `[releng]`.
- **Ours (additive — `ours`):** `scripts-pwpp/` (build/run orchestration), `doc-pwpp/`
  (fork docs), `ai/` (this knowledge layer), `.github/workflows/` (GitHub Actions CI),
  `.claude/` + `.vscode/` + `papyrus.code-workspace`. The most recent commits
  (`88f03f69`, `d322e40f`, `1fea5c75`) are ours — build scripts, onboarding, VS Code config.
- **Upstream convention markers:** commit prefixes `GL-###` (GitLab issues), `[releng]`,
  "Switch to Sirius Web 20xx.x" = upstream lineage. `.gitlab-ci.yml` is upstream's CI.

## Glossary  `[inferred]`
| Term | Meaning here |
|---|---|
| **pwpp** | papyrus-web-**p**lus-**p**lus — this fork; suffix marking fork-owned dirs (`scripts-pwpp`, `doc-pwpp`) |
| **Sirius Components / Sirius Web** | Eclipse low-code platform this app is built on; supplies the diagram/form/tree engine and GraphQL API |
| **Representation** | A Sirius view over the model — e.g. a UML diagram; built by `papyrus-web-representation-builder` |
| **Profile** | A UML profile (C++, Java, CodeGen, Transformation) adding stereotypes/semantics; one Maven module each |
| **Custom node / custom widget** | Papyrus extensions to Sirius' default diagram nodes and property-form widgets |
| **Editing context** | Sirius runtime concept: a loaded model + its representations a user edits (Sirius term) |
| **Fat JAR** | The single runnable backend artifact that also serves the bundled React UI on :8080 |
