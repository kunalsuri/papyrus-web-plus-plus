<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Feature Catalog — Backend Details for papyrus-web-plus-plus

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

## §1 Backend Module Map & Key Classes

| Module / Package | Key Class / Service | Layer / Responsibility | Status |
|---|---|---|---|
| `backend/papyrus-web-application` | `[UMLProfileService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/services/UMLProfileService.java)` | Service layer for Profile actions. | `?` |
| `backend/papyrus-web-application` | `[UMLStereotypeService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/services/UMLStereotypeService.java)` | Service layer for Stereotype application. | `?` |
| `backend/papyrus-web-application` | `[PapyrusUMLLibraryPublisher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusUMLLibraryPublisher.java)` | Service for packaging UML libraries. | `?` |
| `backend/papyrus-web-application` | `[PapyrusResourceService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusResourceService.java)` | Loading external resources and library imports. | `?` |
| `backend/papyrus-web-domain` | `[ProfileResourceEntity.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-domain/src/main/java/org/eclipse/papyrus/web/domain/boundedcontext/profile/ProfileResourceEntity.java)` | Database-mapped profile entity representation. | `?` |
| `backend/papyrus-web-infra` | `[PapyrusWeb.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web/src/main/java/org/eclipse/papyrus/web/PapyrusWeb.java)` | Main Spring Boot application bootstrap loader. | `?` |

---

## §2 Database Schema & Persistence

| Table / Collection | Key Model | Description |
|---|---|---|
| `profile` | `[ProfileResourceEntity](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-domain/src/main/java/org/eclipse/papyrus/web/domain/boundedcontext/profile/ProfileResourceEntity.java)` | Stores dynamic dynamic/custom UML profile contents as XML payloads mapped by UUID keys. |

---

## §3 Backend Verification Tests

| Test Class / Path | Target Component | Description |
|---|---|---|
| `[PRDDiagramCreationTest.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web/src/test/java/org/eclipse/papyrus/web/tools/profile/PRDDiagramCreationTest.java)` | Profile Diagram creation tools | Verifies creation of profile diagrams and stereotype definitions. |
| `[CustomImageTest.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-custom-widgets/src/test/java/org/eclipse/papyrus/web/custom/widgets/customimage/CustomImageTest.java)` | Custom Image Widget | Tests custom image widget properties pane bindings. |
| `[LanguageExpressionComponentTest.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-custom-widgets/src/test/java/org/eclipse/papyrus/web/custom/widgets/languageexpression/LanguageExpressionComponentTest.java)` | Language Expression Widget | Verifies language expression widget properties mapping. |
