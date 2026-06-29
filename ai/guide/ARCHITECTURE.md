<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Architecture — papyrus-web-plus-plus

> Status: drafted by `/cold-start` on 2026-06-29; every section `[inferred]` until a human audits it.

## The big pieces  `[inferred]`
- [frontend/papyrus-web/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web): The main React single-page application (SPA) providing the UML modeler UI using Eclipse Sirius Web Components.
- [frontend/papyrus-web-components/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components): Shared React components consumed by the modeler application.
- [backend/papyrus-web/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web): Bootable Spring Boot application serving as the web backend and hosting the static assets of the frontend.
- [backend/papyrus-web-application/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application): The Sirius integration and assembly layer that hooks representation builders, property builders, domain models, and profiles together.
- [backend/papyrus-web-domain/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-domain): UML domain model layer that defines the core UML meta-models and concepts.
- [backend/papyrus-web-sirius-contributions/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-sirius-contributions): Base integration and extension interfaces layered on top of Eclipse Sirius Components.
- [backend/papyrus-web-customnodes/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-customnodes) & [backend/papyrus-web-custom-widgets/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-custom-widgets): Custom extensions to Sirius representing custom diagram nodes and property form widgets.
- [scripts-pwpp/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/scripts-pwpp): Orchestration scripts for local development setup, PostgreSQL container startup, building, and running.

## How they connect  `[inferred]`
- **Frontend ↔ Backend Protocol:** The frontend communicates with the backend via GraphQL queries and mutations over HTTP, and receives real-time updates and collaboration synchronization through WebSockets (`subscriptions-transport-ws`).
- **Backend ↔ Database:** The Spring Boot backend uses Spring Data JPA (Hibernate) to persist UML models, representations, and configurations to a PostgreSQL 15 database running in a Docker container (exposed on port `5439`).
- **Asset Packaging Seam:** The frontend is compiled via Vite/Turbo into `frontend/papyrus-web/dist/`, which is copied during the build phase into `backend/papyrus-web-frontend/src/main/resources/static/` to be served statically by the backend server.

## Diagrams
Text-based (Mermaid) diagrams live in [ai/analysis/diagrams/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/ai/analysis/diagrams/). Regenerate them via `/cold-start`; do not hand-maintain.

## Invariants an agent must not break  `[verified] required`
*(No human-defined invariants have been documented yet. Only humans may add rows to this section.)*
