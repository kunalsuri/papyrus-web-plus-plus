<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Conventions — how to write code that fits papyrus-web-plus-plus

> Status: drafted by `/cold-start` on 2026-06-29; every section `[inferred]` until a human audits it.

## Languages & style  `[inferred]`
- **Java 21 (Backend):** Styling is governed by Checkstyle. The configuration resides in [CheckstyleConfiguration.xml](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-resources/checkstyle/CheckstyleConfiguration.xml). Eclipse cleanups and formatter profiles are located in the resource directory.
- **TypeScript / React 18 (Frontend):** Formatted using Prettier as defined in [.prettierrc](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/.prettierrc). The `format-lint` check is run automatically on build and fails on unformatted code. Use `npm run format` to auto-format before building.

## Patterns to follow  `[inferred]`
- **Backend Mutations & Queries (Sirius Components):** Implement discrete data-fetcher classes (e.g. extending `MutationDeletePrimitiveListItemDataFetcher` or implementing `DataFetcher`). See [NewValueDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-custom-widgets/src/main/java/org/eclipse/papyrus/web/custom/widgets/primitiveradio/datafetchers/NewValueDataFetcher.java) or [UMLImportLibraryCommandProvider.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLImportLibraryCommandProvider.java) for implementation patterns.
- **Frontend Components:** Separate MUI presentation components from GraphQL data fetching. See [ProfilesPage.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesPage.tsx) for layout and query integration patterns.

## Things that look wrong but are right  `[verified] required`
*(No human-defined exceptions/rules have been documented yet. Only humans may add rows to this section.)*

## Definition of done
- Builds: `powershell -File .\scripts-pwpp\build-all.ps1`
- Tests pass: `powershell -File .\scripts-pwpp\build-all.ps1 -WithTests` (runs both backend Maven verification and frontend unit/Cypress E2E test suites)
- License headers match neighbors; diffs are surgical; `ai/` knowledge is updated and tagged `[inferred]` if any layout or feature mappings changed.
