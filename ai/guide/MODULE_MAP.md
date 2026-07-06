<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Module map — directory → responsibility → entry point

> Drafted by `/cold-start` on 2026-07-06 @ commit `6e9b3cf2`; audited and approved by the
> maintainer on 2026-07-06 (tag flips recorded by the agent at the maintainer's explicit direction).

> **Index only.** Find the area here, then open the entry file directly. Don't crawl
> the tree. The directory list can be regenerated; **Responsibility** and **Stability**
> are judgement and must be audited by a human.
> Last verified: 2026-07-06 @ commit 6e9b3cf2

## Stability legend (the most important column)
- `frozen` — inherited / load-bearing legacy. **DO NOT edit** without explicit instruction.
- `stable` — works; change carefully and with tests.
- `ours`   — active development surface. Safe for agents to modify.
- `?`      — not yet audited. **Treat as `frozen` until a human decides.**

## Modules — top level  `[verified] (2026-07-06)`

All `frozen` rows follow the fork rule (inherited from upstream ⇒ hands off), confirmed
by the maintainer on 2026-07-06. Evidence: `git diff upstream-master...main`
shows the fork changed **no upstream source at all** (only deleted Eclipse IDE metadata).

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `backend/` | Upstream Java 21 / Spring Boot / Sirius Components 2026.5.0 server — Maven multi-module (22 modules, detailed below) | `backend/pom.xml` | frozen |
| `frontend/` | Upstream npm+turbo workspace: React 18 SPA + shared component lib (details below) | `frontend/package.json` | frozen |
| `integration-tests/` | Upstream Cypress e2e suite; runs against `http://localhost:8080` (backend-served app, not Vite) | `integration-tests/cypress.config.ts` | frozen |
| `doc/` | Upstream documentation (user, dev, deploy, spec, test-campaign) | `doc/` | frozen |
| `scripts/` | Upstream utility scripts | `scripts/` | frozen |
| `dockerfiles/` | Upstream Docker image definition | `dockerfiles/Dockerfile` | frozen |
| `scripts-pwpp/` | OURS — dev workflow: one-shot setup, full build, start/stop dev stack (DB → backend → frontend) | `scripts-pwpp/start-dev.ps1` | ours |
| `doc-pwpp/` | OURS — fork docs: pristine-mirror fork strategy, first-build troubleshooting | `doc-pwpp/FORK-SETUP.md` | ours |
| `ai/` | OURS — AI knowledge layer (this folder) | `ai/INDEX.md` | ours |
| `.claude/` `.agents/` `.cursor/` | OURS — per-tool agent configs, rules, skills, workflows | `.claude/rules/ai-knowledge-layer.md` | ours |
| `.github/` | MIXED — upstream CI (`.github/workflows/build-papyrus-app.yml`) + OUR AI additions (chatmodes, prompts, `ai-check.yml`) — UNSURE, needs human | `.github/workflows/` | ? |

## Backend Maven modules  `[verified] (2026-07-06)`

From `backend/pom.xml` `<modules>`. Responsibilities marked *(name-only)* were guessed
from the module name alone — verify before relying on them.

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `backend/papyrus-web/` | Executable Spring Boot module; assembles everything into the runnable fat JAR | `backend/papyrus-web/src/main/java/org/eclipse/papyrus/web/PapyrusWeb.java` | frozen |
| `backend/papyrus-web-application/` | Application layer (~224 files): GraphQL data fetchers/controllers, UML services, representations, profiles, templates, properties, explorer, pathmaps, read-only | `backend/papyrus-web-application/pom.xml` | frozen |
| `backend/papyrus-web-domain/` | Domain layer: `profile` bounded context — entity, repository, creation/deletion/search services, events | `backend/papyrus-web-domain/pom.xml` | frozen |
| `backend/papyrus-web-parent/` | Maven parent POM: Java 21, Spring Boot parent 4.0.5, Sirius Components 2026.5.0 version management | `backend/papyrus-web-parent/pom.xml` | frozen |
| `backend/papyrus-web-frontend/` | Packages the built frontend `dist/` as static web resources served by the JAR | `backend/papyrus-web-frontend/pom.xml` | frozen |
| `backend/papyrus-web-sirius-contributions/` | Extensions/overrides contributed into the Sirius Web framework *(name-only)* — UNSURE | `backend/papyrus-web-sirius-contributions/pom.xml` | frozen |
| `backend/papyrus-web-representation-builder/` | Builders for diagram/representation descriptions *(name-only)* | `backend/papyrus-web-representation-builder/pom.xml` | frozen |
| `backend/papyrus-web-properties-builder/` | Builders for properties-view descriptions *(name-only)* | `backend/papyrus-web-properties-builder/pom.xml` | frozen |
| `backend/papyrus-web-graphics/` | Graphics/icon resources *(name-only)* — UNSURE | `backend/papyrus-web-graphics/pom.xml` | frozen |
| `backend/papyrus-web-infra/` | Infrastructure utilities *(name-only)* — UNSURE | `backend/papyrus-web-infra/pom.xml` | frozen |
| `backend/papyrus-web-customnodes/` + `backend/papyrus-web-customnodes-edit/` | Custom diagram node model + its EMF `.edit` support *(name-only)* | `backend/papyrus-web-customnodes/pom.xml` | frozen |
| `backend/papyrus-web-custom-widgets/` + `backend/papyrus-web-custom-widgets-view/` + `backend/papyrus-web-custom-widgets-view-edit/` | Custom form widgets + their View-DSL model and EMF `.edit` support *(name-only)* | `backend/papyrus-web-custom-widgets/pom.xml` | frozen |
| `backend/papyrus-web-cpp-profile/` `-java-profile/` `-codegen-profile/` `-transformation-profile/` | Bundled UML profiles: C++, Java, code generation, transformation *(name-only)* | `backend/papyrus-web-cpp-profile/pom.xml` | frozen |
| `backend/papyrus-web-tests/` | Shared test infrastructure/suites *(name-only)* — UNSURE | `backend/papyrus-web-tests/pom.xml` | frozen |
| `backend/papyrus-web-test-coverage/` | Coverage aggregation module *(name-only)* | `backend/papyrus-web-test-coverage/pom.xml` | frozen |
| `backend/papyrus-web-resources/` | On disk but **absent from `backend/pom.xml` `<modules>`** — UNSURE what it is, needs human | — | ? |

## Frontend packages  `[verified] (2026-07-06)`

npm workspaces (`frontend/package.json`), built with turbo; Node 22.16.0 / npm 10.9.2 pinned.

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `frontend/papyrus-web/` | The Papyrus Web SPA: wraps `@eclipse-sirius/sirius-web-application` (Apollo GraphQL inside), Vite build, vitest tests | `frontend/papyrus-web/src/index.tsx` | frozen |
| `frontend/papyrus-web-components/` | Shared React component library consumed by the app (`yalc` for local publish) | `frontend/papyrus-web-components/package.json` | frozen |

Detected test locations (from orient): integration-tests/

## Audit protocol
1. /cold-start fills rows and tags them `[inferred]`.
2. A human sets Stability per row and flips confirmed rows to `[verified] (date)`.
3. Agents treat `?` rows as `frozen`. Agents never flip tags.

Field guide for the human audit (how to decide, evidence bar, worked rows):
https://github.com/kunalsuri/ai-fication-kit/blob/main/docs/AUDIT-GUIDE.md
