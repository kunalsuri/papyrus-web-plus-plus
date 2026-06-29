<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Module map — directory → responsibility → entry point

> **Index only.** Find the area here, then open the entry file directly. Don't crawl
> the tree. The directory list can be regenerated; **Responsibility** and **Stability**
> are judgement and must be audited by a human.
> Last drafted: 2026-06-29 by `/cold-start` @ commit `88f03f69` — **all rows `[inferred]`, awaiting human audit**

## Stability legend (the most important column)
- `frozen` — inherited / load-bearing legacy. **DO NOT edit** without explicit instruction.
- `stable` — works; change carefully and with tests.
- `ours`   — active development surface. Safe for agents to modify.
- `?`      — not yet audited. **Treat as `frozen` until a human decides.**

> [!IMPORTANT]
> **Fork boundary (the central fact of this repo).** This is a fork of
> `papyrus/org.eclipse.papyrus-web`. Every module under `backend/`, `frontend/`,
> `integration-tests/`, `doc/`, and `scripts/` carries **upstream** naming
> (`papyrus-web-*`, `@eclipse-papyrus/*`) — there is **no `-pwpp` suffix inside the
> source trees**, so any fork-specific edits are **interleaved in-place** inside
> upstream modules, not isolated. Therefore every source module is tagged `frozen`
> by default. **Our additive, fork-owned surface** is everything with a `-pwpp`
> suffix plus `ai/`, `.github/`, `.claude/`, `.vscode/` (tagged `ours`). A human must
> confirm which `frozen` modules actually contain fork edits (use `git log`/`git blame`
> filtered to non-`GL-###` commits).

## Backend — `backend/` (Java 21 · Maven 3.9.9 reactor · Spring Boot on Eclipse Sirius Components)

Ordered roughly bottom-up by the dependency graph (see `ai/analysis/diagrams/package-deps.mmd`).

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `backend/` | Maven reactor root (artifactId `papyrus-web-container`) — aggregates all modules below | `backend/pom.xml` | frozen `[inferred]` |
| `backend/papyrus-web-parent` | Parent POM: dependency management, plugin/build config, Sirius & Papyrus Maven repos | `backend/papyrus-web-parent/pom.xml` | frozen `[inferred]` |
| `backend/papyrus-web-resources` | **Not a Maven module** — shared build/quality config (Checkstyle, Eclipse formatter, cleanup profile, code templates) | `backend/papyrus-web-resources/checkstyle/CheckstyleConfiguration.xml` | frozen `[inferred]` |
| `backend/papyrus-web-domain` | UML domain model layer ("Papyrus Web Domain"); leaf, parent-only deps | `backend/papyrus-web-domain/` | frozen `[inferred]` |
| `backend/papyrus-web-sirius-contributions` | Base integration/extension points layered on Eclipse Sirius Components | `backend/papyrus-web-sirius-contributions/` | frozen `[inferred]` |
| `backend/papyrus-web-tests` | Shared test utilities/fixtures reused across modules | `backend/papyrus-web-tests/` | frozen `[inferred]` |
| `backend/papyrus-web-graphics` | Icons and high-res graphics for the UML editor | `backend/papyrus-web-graphics/` | frozen `[inferred]` |
| `backend/papyrus-web-customnodes` | Backend support for additional custom diagram nodes | `backend/papyrus-web-customnodes/` | frozen `[inferred]` |
| `backend/papyrus-web-customnodes-edit` | EMF edit support for custom nodes (→ `customnodes`) | `backend/papyrus-web-customnodes-edit/` | frozen `[inferred]` |
| `backend/papyrus-web-custom-widgets` | Custom property/form widgets (→ `-view`, `-view-edit`) | `backend/papyrus-web-custom-widgets/` | frozen `[inferred]` |
| `backend/papyrus-web-custom-widgets-view` | View-model support for the custom widgets | `backend/papyrus-web-custom-widgets-view/` | frozen `[inferred]` |
| `backend/papyrus-web-custom-widgets-view-edit` | EMF edit support for the custom-widgets view (→ `-view`) | `backend/papyrus-web-custom-widgets-view-edit/` | frozen `[inferred]` |
| `backend/papyrus-web-representation-builder` | Builds Sirius **representation** descriptions, i.e. UML diagrams (→ sirius-contributions, customnodes*) | `backend/papyrus-web-representation-builder/` | frozen `[inferred]` |
| `backend/papyrus-web-properties-builder` | Builds **property-view (form)** descriptions (→ custom-widgets). ⚠ pom `<description>` mislabels it "Representation Builder" — verify | `backend/papyrus-web-properties-builder/` | frozen `[inferred]` |
| `backend/papyrus-web-application` | **Wiring/assembly layer** — assembles representations, properties, widgets, nodes, graphics & domain into Sirius (NOT the bootable app) | `backend/papyrus-web-application/` | frozen `[inferred]` — role needs human |
| `backend/papyrus-web-cpp-profile` | UML **C++** profile support (→ application, representation-builder) | `backend/papyrus-web-cpp-profile/` | frozen `[inferred]` |
| `backend/papyrus-web-java-profile` | UML **Java** profile support (→ application, representation-builder, sirius-contributions) | `backend/papyrus-web-java-profile/` | frozen `[inferred]` |
| `backend/papyrus-web-codegen-profile` | UML **CodeGen** profile support (→ application) | `backend/papyrus-web-codegen-profile/` | frozen `[inferred]` |
| `backend/papyrus-web-transformation-profile` | UML **Transformation** profile support (→ application) | `backend/papyrus-web-transformation-profile/` | frozen `[inferred]` |
| `backend/papyrus-web-infra` | Cross-cutting infrastructure for the server (→ application) | `backend/papyrus-web-infra/` | frozen `[inferred]` |
| `backend/papyrus-web-frontend` | Packages the built React UI as Spring **static resources** — `build-all.ps1` copies `frontend/papyrus-web/dist/*` into `src/main/resources/static/` here | `backend/papyrus-web-frontend/pom.xml` | frozen `[inferred]` — **build target** |
| `backend/papyrus-web` | **Bootable Spring Boot server** (`@SpringBootApplication`); pulls in infra, frontend assets & all 4 UML profiles | `backend/papyrus-web/src/main/java/org/eclipse/papyrus/web/PapyrusWeb.java` | frozen `[inferred]` |
| `backend/papyrus-web-test-coverage` | JaCoCo coverage aggregator across the reactor | `backend/papyrus-web-test-coverage/pom.xml` | frozen `[inferred]` |

## Frontend — `frontend/` (TypeScript · Vite 8 · React 18 · npm workspaces + turbo 2.4.4)

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `frontend/` | npm + turbo monorepo root; workspaces build `papyrus-web-components` then `papyrus-web` | `frontend/package.json` | frozen `[inferred]` |
| `frontend/papyrus-web-components` | Shared React component library (`@eclipse-papyrus/papyrus-web-components`) consumed by the app | `frontend/papyrus-web-components/` | frozen `[inferred]` |
| `frontend/papyrus-web` | **Main UML modeler SPA** (`@eclipse-papyrus/papyrus-web`); Sirius Components + Apollo GraphQL client, MUI, `@xyflow/react` canvas | `frontend/papyrus-web/src/index.tsx` (seam: `src/core/URL.ts`) | frozen `[inferred]` |

## Tests & shared

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `integration-tests/` | End-to-end tests (Cypress — recent commits reference Cypress fixes) | `integration-tests/` | frozen `[inferred]` — needs human |
| `doc/` | Upstream developer/user documentation | `doc/` | frozen `[inferred]` |
| `scripts/` | Upstream build/release helper scripts | `scripts/` | frozen `[inferred]` |

## OUR fork-owned surface (`ours` — safe to modify)

| Directory | Responsibility (one line) | Entry point | Stability |
|---|---|---|---|
| `scripts-pwpp/` | **OURS** — PowerShell/batch build & run orchestration (`setup`, `build-all`, `start-db/backend/frontend/dev`) | `scripts-pwpp/build-all.ps1` | ours `[inferred]` |
| `doc-pwpp/` | **OURS** — fork onboarding & build-troubleshooting docs | `doc-pwpp/INITIAL-BUILD-TROUBLESHOOTING.md` | ours `[inferred]` |
| `ai/` | **OURS** — AI knowledge layer (this kit): guides, analysis, diagrams, lab | `ai/INDEX.md` | ours `[inferred]` |
| `.github/workflows/` | **OURS** — GitHub Actions CI for the fork (upstream uses `.gitlab-ci.yml`) | `.github/workflows/build-papyrus-app.yml` | ours `[inferred]` |
| `.claude/` · `.vscode/` · `papyrus.code-workspace` | **OURS** — agent + editor configuration | — | ours `[inferred]` |

Detected test locations (from orient): integration-tests/

## Audit protocol
1. /cold-start fills rows and tags them `[inferred]`.
2. A human sets Stability per row and flips confirmed rows to `[verified] (date)`.
3. Agents treat `?` rows as `frozen`. Agents never flip tags.

Field guide for the human audit (how to decide, evidence bar, worked rows):
https://github.com/kunalsuri/ai-fication-kit/blob/main/docs/AUDIT-GUIDE.md
