<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Feature map — feature → files, intent, gotchas

> Candidates drafted by `/cold-start` on 2026-07-06 — all `[inferred]`, audit before trusting.
> `ai/analysis/FEATURE_CATALOG.md` is still a placeholder; run `/create-feature-catalog` to mine the full list.

> Humans think in features; agents should too. This file holds the SHORT version —
> per-feature pointers and non-obvious notes. The full generated catalog lives in
> `ai/analysis/FEATURE_CATALOG.md` (via /create-feature-catalog).

## Template (copy per feature)

### <Feature name>  `[inferred]`
- **Business goal:** <one line>
- **Touches:** <dirs/files across layers — UI, backend, persistence, tests>
- **Verify with:** <the specific test command or suite>
- **Gotchas:** <the non-obvious thing that bites people>
- **Related:** <other features that share code paths>

## Candidate features (drafted by /cold-start, audit before trusting)

All upstream features below live in **frozen** code — they are listed so agents can *find*
them, not edit them. "Verify with" commands are best guesses; the exact suite per feature
is UNSURE — needs human.

### UML Profile management (create/publish/apply)  `[inferred]`
- **Business goal:** manage UML profiles and apply them to models from the web UI (upstream GL-323 added a Profiles page).
- **Touches:** `backend/papyrus-web-domain/` (profile bounded context), `backend/papyrus-web-application/` (`profile/controllers`, e.g. `MutationApplyProfileDataFetcher`), `frontend/papyrus-web/src/profiles/` (`ProfilesPage.tsx`, `DisplayProfileView.tsx`)
- **Verify with:** backend `mvn clean verify` (from `backend/`); UNSURE which suite specifically
- **Gotchas:** the only bounded context in `papyrus-web-domain` — most other domain logic comes from the external `papyrus-uml-domain-services` dependency.
- **Related:** bundled language profiles below.

### Diagram / representation editing  `[inferred]`
- **Business goal:** UML diagrams (Sirius Components diagrams) with Papyrus-specific node types.
- **Touches:** `backend/papyrus-web-application/` (`representations/` — aqlservices, controllers, handlers, nodes), `backend/papyrus-web-customnodes/`, `backend/papyrus-web-representation-builder/`, frontend via `@eclipse-sirius/sirius-components-diagrams`
- **Verify with:** backend `mvn clean verify`; Cypress in `integration-tests/`
- **Gotchas:** rendering logic largely lives in the external Sirius Components packages, not this repo.
- **Related:** custom widgets, properties views.

### Properties views & custom widgets  `[inferred]`
- **Business goal:** edit UML element properties in detail forms with Papyrus-specific widgets.
- **Touches:** `backend/papyrus-web-application/` (`properties/`), `backend/papyrus-web-properties-builder/`, `backend/papyrus-web-custom-widgets/` (+`-view`, `-view-edit`)
- **Verify with:** backend `mvn clean verify`
- **Gotchas:** UNSURE how the View-DSL models in `-view` modules are generated (EMF codegen?) — needs human.
- **Related:** diagram editing.

### Bundled language profiles (C++/Java/codegen/transformation)  `[inferred]`
- **Business goal:** ship ready-made UML profiles for code-oriented modeling.
- **Touches:** `backend/papyrus-web-cpp-profile/`, `backend/papyrus-web-java-profile/`, `backend/papyrus-web-codegen-profile/`, `backend/papyrus-web-transformation-profile/`
- **Verify with:** backend `mvn clean verify`
- **Gotchas:** *(name-only guess — modules not read)*
- **Related:** UML Profile management.

### Project templates & model import  `[inferred]`
- **Business goal:** bootstrap projects/documents from templates; import libraries (upstream GL-312/GL-283 added Import/Publish Library).
- **Touches:** `backend/papyrus-web-application/` (`templates/`, `document/controllers`, `pathmap/services`)
- **Verify with:** backend `mvn clean verify`
- **Gotchas:** UNSURE where library publish/import state is persisted.
- **Related:** explorer tree (`explorer/builder`), read-only mode (`readonly/services`).

### Dev workflow scripts (OURS)  `[inferred]`
- **Business goal:** one-command setup/build/run of the whole stack on a dev machine.
- **Touches:** `scripts-pwpp/` (`setup-dev.ps1`, `build-all.ps1`, `start-dev.ps1`, `stop-dev.ps1`, `shared_core_lib.ps1`, `docker-compose.dev.yml`)
- **Verify with:** `.\scripts-pwpp\setup-dev.ps1 -CheckOnly`; then `.\scripts-pwpp\start-dev.ps1`
- **Gotchas:** turbo `start` does NOT build `papyrus-web-components` — `start-frontend.ps1` compensates by building its dist first if missing; DB creds/ports are single-sourced in `shared_core_lib.ps1`.
- **Related:** everything (it builds and runs the two frozen trees).
