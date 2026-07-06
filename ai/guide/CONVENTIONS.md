<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Conventions — how to write code that fits papyrus-web-plus-plus

> Status: drafted by /cold-start (2026-07-06) `[inferred]`; humans
> confirm and add the rules that live only in heads.

## Languages & style  `[inferred]`
- Java 21 (backend), TypeScript 5.4 + React 18 (frontend), PowerShell 7 (`scripts-pwpp/`).
- Frontend formatting: Prettier — configs at `frontend/.prettierrc`, `frontend/papyrus-web/.prettierrc`, `frontend/papyrus-web-components/.prettierrc`, `integration-tests/.prettierrc`. Run `npm run format` (per workspace) or `.\scripts-pwpp\build-all.ps1 -FixFormat`; CI-style check via `npm run format-lint`.
- Backend style: upstream README has a "Coding rules" → "Headers" section (license headers required on every file); Java linter/checkstyle specifics UNSURE — needs human.
- License headers: copy from a neighboring file in the same module — upstream files carry Eclipse (EPL-2.0-style) headers, our fork files carry `Kunal Suri (CEA LIST)` headers. Never invent a new header form.

## Patterns to follow  `[inferred]`
- GraphQL mutation/query resolvers follow the Sirius Components `IDataFetcher` pattern — exemplar: `backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyProfileDataFetcher.java`.
- Domain code is organized as bounded contexts (`entity` / `repositories` / `service` / `service.api` / `events`) — exemplar: `backend/papyrus-web-domain/src/main/java/org/eclipse/papyrus/web/domain/boundedcontext/profile/`.
- PowerShell scripts dot-source `scripts-pwpp/shared_core_lib.ps1` — the single source of truth for ports, paths, and DB credentials; never hardcode those elsewhere.
- Frontend app code extends the `SiriusWebApplication` shell rather than re-implementing routing/clients — exemplar: `frontend/papyrus-web/src/index.tsx`.
- New fork work goes in `-pwpp`-suffixed directories or `ai/`/agent-config trees — never in the frozen upstream trees (see `ai/guide/MODULE_MAP.md`).

## Things that look wrong but are right  `[verified] required`
<Only humans add rows. The institutional knowledge that prevents "helpful" breakage.>

## Definition of done
- Builds: `.\scripts-pwpp\build-all.ps1` (or manual: `npm ci && npm run build` in `frontend/`, then `mvn clean verify -DskipTests` in `backend/`) `[inferred]`
- Tests pass: `mvn clean verify` in `backend/` for backend changes; `npm run test` in `frontend/papyrus-web/` for frontend changes; Cypress (`integration-tests/`) for end-to-end `[inferred]`
- License headers match neighbors; diffs are surgical; ai/ knowledge updated if the
  change moved or added modules/features.
