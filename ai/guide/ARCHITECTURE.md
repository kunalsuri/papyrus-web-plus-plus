<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Architecture — papyrus-web-plus-plus

> Status: drafted by /cold-start (2026-07-06); every claim `[inferred]` until a human
> audits it. Tag every claim `[inferred]` or `[verified] (date)`.

## The big pieces  `[inferred]`
- **`frontend/papyrus-web/`** — React 18 SPA; a thin shell around `@eclipse-sirius/sirius-web-application` 2026.5.0 (which owns routing, Apollo GraphQL client, diagram/tree/form components). Entry: `frontend/papyrus-web/src/index.tsx`.
- **`frontend/papyrus-web-components/`** — shared React component library, the second npm workspace; consumed by the app.
- **`backend/papyrus-web/`** — executable Spring Boot module (main class `org.eclipse.papyrus.web.PapyrusWeb`); packages everything into one fat JAR.
- **`backend/papyrus-web-application/`** — the application layer: Sirius Components GraphQL data fetchers/controllers, UML services, representations, profiles, templates, properties.
- **`backend/papyrus-web-domain/`** — domain layer holding the `profile` bounded context; the broader UML domain logic comes from the external `papyrus-uml-domain-services` dependency (UNSURE — needs human).
- **PostgreSQL 15** — persistence; dev instance via `scripts-pwpp/docker-compose.dev.yml` on `localhost:5439` (`papyrus-web-db`).
- **`scripts-pwpp/`** *(ours)* — PowerShell dev workflow that builds and orchestrates all of the above (DB → backend → frontend).

## How they connect  `[inferred]`
- **Frontend ↔ backend: GraphQL over HTTP plus GraphQL subscriptions over WebSocket.** OBSERVED: `graphql` 16.8.1, `@apollo/client` 3.10.4, `subscriptions-transport-ws` 0.11.0 in `frontend/papyrus-web/package.json`; `wsOrigin` passed to `SiriusWebApplication` in `frontend/papyrus-web/src/index.tsx`; `IDataFetcher`-pattern classes in `backend/papyrus-web-application/` (e.g. `MutationApplyProfileDataFetcher.java`). The exact endpoint paths are managed inside the Sirius Web library — UNSURE, needs human.
- **Backend ↔ database:** JDBC `jdbc:postgresql://localhost:5439/papyrus-web-db` in dev (OBSERVED in `scripts-pwpp/`); pooling/config specifics UNSURE — needs human.
- **Build-time seam:** the frontend `dist/` is copied into `backend/papyrus-web-frontend/src/main/resources/static` so the JAR serves the SPA at `:8080`; in dev, Vite serves it at `:5173` instead. Cypress (`integration-tests/`) targets `:8080`, i.e. the JAR-served app.
- **Fork seam:** `main` vs `upstream-master` (pristine GitLab mirror — see `doc-pwpp/FORK-SETUP.md`). OBSERVED via `git diff upstream-master...main`: the fork changes no upstream source; our delta is AI/agent config, `scripts-pwpp/`, `doc-pwpp/`, and governance docs.

## Diagrams
Text-based (Mermaid) diagrams live in `ai/analysis/diagrams/`. Regenerate them via
/cold-start; do not hand-maintain.

## Invariants an agent must not break  `[verified] required`
<Only humans add rows here. Examples: "generated code in X is never hand-edited",
"public API schemas are backwards compatible".>
