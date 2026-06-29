<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Feature Catalog — Frontend Details for papyrus-web-plus-plus

---

> ## Provenance & scope
>
> **Status: [inferred].** This catalog is populated on demand by `/create-feature-catalog` on 2026-06-29.
>
> **Confidence key used throughout (same scheme as `ai/INDEX.md`):**
> - `[inferred]` — written by an agent or tool; a guess until a human checks it
> - `[verified]` — a human confirmed it, with the date. Agents never set this tag.
> - `?` in Status column — requires a human decision/audit

---

## §1 Frontend Components & Views

| Page / Component | Path | Responsibility | Status |
|---|---|---|---|
| `ProfilesPage` | `[ProfilesPage.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesPage.tsx)` | Main profiles management page UI dashboard. | `?` |
| `DisplayProfileView` | `[DisplayProfileView.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/DisplayProfileView.tsx)` | Detailed view for managing versions and content of specific profiles. | `?` |
| `ApplyProfileModal` | `[ApplyProfileModal.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/ApplyProfileModal.tsx)` | Dialog component to search, select, and apply profiles. | `?` |
| `ApplyStereotypeModal` | `[ApplyStereotypeModal.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/ApplyStereotypeModal.tsx)` | Dialog component to select and apply stereotypes to elements. | `?` |
| `PublishProfileDialog` | `[PublishProfileDialog.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/publish-profile/PublishProfileDialog.tsx)` | Form Dialog to fill in meta information for publishing profile diagrams. | `?` |
| `PublishUMLLibraryCommand` | `[PublishUMLLibraryCommand.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/libraries/PublishUMLLibraryCommand.tsx)` | UI Omnibox command element for triggering UML library publishing. | `?` |
| `Footer` | `[Footer.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/footer/Footer.tsx)` | Customized application bottom panel. | `?` |

---

## §2 State Management & Hooks

| Hook / Store | Path | Responsibility |
|---|---|---|
| `ApplyProfileModalMachine` | `[ApplyProfileModalMachine.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/ApplyProfileModalMachine.tsx)` | XState machine orchestrating the interactive steps of applying a profile. |
| `ApplyStereotypeModalMachine` | `[ApplyStereotypeModalMachine.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/ApplyStereotypeModalMachine.ts)` | XState machine orchestrating the stereotyping options and form interactions. |

---

## §3 Frontend Verification Tests

| Test File | Target Component | Description |
|---|---|---|
| `integration-tests/cypress/e2e/` | UML Modeler layout & features | End-to-end Cypress integration tests validating user navigation, diagram shapes, context menus, and profiles. |
