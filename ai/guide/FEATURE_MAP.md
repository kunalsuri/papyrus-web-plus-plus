<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Feature map — feature → files, intent, gotchas

> Status: drafted by `/cold-start` on 2026-06-29; every section `[inferred]` until a human audits it.

## Active features

> [!NOTE]
> The complete list of features, touch lists, 3-file rules, and verification paths is detailed in the [Feature Catalog](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/ai/analysis/FEATURE_CATALOG.md).

### Profiles Page  `[inferred]`
- **Business goal:** Manage UML profiles (Java, C++, CodeGen, Transformation) and associate them with UML modeling projects.
- **Touches:**
  - UI Component: [ProfilesPage.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesPage.tsx), [DisplayProfileView.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/DisplayProfileView.tsx)
  - Navigation Entry: [ProfilesMenuItem.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesMenuItem.tsx), [index.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/index.tsx)
  - Backend integration: [backend/papyrus-web-cpp-profile/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-cpp-profile), [backend/papyrus-web-java-profile/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-java-profile), [backend/papyrus-web-codegen-profile/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-codegen-profile), [backend/papyrus-web-transformation-profile/](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-transformation-profile)
- **Verify with:** Verify UI navigation manually; run backend profile module Maven builds (`mvn clean compile` in profile dirs).
- **Gotchas:** Profiles must align with Sirius representation definitions to load stereotypes correctly.

### Import Library  `[inferred]`
- **Business goal:** Allow users to import external UML libraries/models into the current editing context.
- **Touches:**
  - UI Registry: [PapyrusWebExtensionRegistry.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusWebExtensionRegistry.tsx)
  - Backend Command Provider: [UMLImportLibraryCommandProvider.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLImportLibraryCommandProvider.java)
- **Verify with:** Integration/E2E Cypress tests or backend tests in `papyrus-web-tests`.
- **Gotchas:** Requires resolving dependent package schemas correctly, otherwise imported elements might show as broken references.

### Publish Library  `[inferred]`
- **Business goal:** Export the semantic contents of a UML model to make it available as a library.
- **Touches:**
  - UI Button/Component: [PublishUMLLibraryCommand.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/libraries/PublishUMLLibraryCommand.tsx)
  - Backend Service & Handler: [PapyrusUMLLibraryPublisher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusUMLLibraryPublisher.java), [UMLLibraryPublicationHandler.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLLibraryPublicationHandler.java)
- **Verify with:** E2E Cypress tests under `integration-tests/`.
- **Gotchas:** Publishing requires correct write-permissions on the destination repository or registry.

### Custom Branding Theme  `[inferred]`
- **Business goal:** Apply a custom, project-specific theme and colors representing Papyrus-Web branding.
- **Touches:**
  - Theme Config: [papyrusTheme.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/theme/papyrusTheme.ts)
  - Layout & Footer overrides: [Footer.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/footer/Footer.tsx), [PapyrusNavigationBarIcon.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/core/PapyrusNavigationBarIcon.tsx)
- **Verify with:** `npm run start` in `frontend/papyrus-web` to check visuals. Run Cypress E2E tests to verify selector styling matches color scheme overrides.
- **Gotchas:** Material-UI style overrides can conflict with Sirius components base styles if themes are updated upstream.
