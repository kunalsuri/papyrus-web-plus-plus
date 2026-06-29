<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
# Feature Catalog — papyrus-web-plus-plus Master Index

---

> ## Provenance & scope
>
> **Status: [inferred].** This catalog is populated on demand by `/create-feature-catalog` on 2026-06-29.
>
> **Confidence key used throughout (same scheme as `ai/INDEX.md`):**
> - `[inferred]` — written by an agent or tool; a guess until a human checks it
> - `[verified]` — a human confirmed it, with the date. Agents never set this tag.
> - `?` in Status column — requires a human decision/audit
>
> **What this file does NOT contain:** planned but unimplemented features ➔ see `ai/lab/specs/`

---

## How to use the catalog

This catalog is split across frontend and backend detail files to keep token context overhead low while keeping this master file as the main navigation layer:

| File | Contains | Load when |
|---|---|---|
| **This file** (`FEATURE_CATALOG.md`) | Feature index, API surface summary, cross-stack touch lists, decision tree | Always — it is the navigation layer |
| [`FEATURE_CATALOG_BACKEND.md`](FEATURE_CATALOG_BACKEND.md) | Detailed backend modules, classes, and architectural roles | Working on backend/services |
| [`FEATURE_CATALOG_FRONTEND.md`](FEATURE_CATALOG_FRONTEND.md) | Detailed frontend components, hooks, routes, and styles | Working on frontend/UI |

---

## §1 Feature Index

| ID | Feature | What it does | Entry point(s) | Related | Status |
|---|---|---|---|---|---|
| F1 | **Profiles Dashboard** | Manage UML profiles (upload, delete, view versions) and associate them with modeling projects. | `[ProfilesPage.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesPage.tsx)` `[inferred]` | F2, F3, F4 | `?` |
| F2 | **Apply UML Profile** | Apply standard or dynamic UML profiles to a UML model. | `[UMLModelTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/UMLModelTreeItemContextMenuContribution.tsx)` `[inferred]` | F1, F3, F4 | `?` |
| F3 | **Apply UML Stereotype** | Apply stereotypes defined by profiles to specific UML semantic elements. | `[UMLElementTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/UMLElementTreeItemContextMenuContribution.tsx)` `[inferred]` | F1, F2 | `?` |
| F4 | **Publish UML Profile** | Export a dynamic profile from the modeling canvas to make it globally available. | `[PublishProfileTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/publish-profile/PublishProfileTreeItemContextMenuContribution.tsx)` `[inferred]` | F1, F2 | `?` |
| F5 | **Import UML Library** | Import standard external UML libraries/models into the editor workspace. | `[UMLImportLibraryCommandProvider.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLImportLibraryCommandProvider.java)` `[inferred]` | F6 | `?` |
| F6 | **Publish UML Library** | Package and publish a UML model so it can be imported elsewhere as a library. | `[PublishUMLLibraryCommand.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/libraries/PublishUMLLibraryCommand.tsx)` `[inferred]` | F5 | `?` |
| F7 | **Custom Diagram Shapes (Nodes)** | Provide customized shapes (Ellipse, Cuboid, Note, Flags, Packages) for modeling elements on the canvas. | `[PapyrusNodeTypeRegistryValue.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusNodeTypeRegistryValue.tsx)` `[inferred]` | F8 | `?` |
| F8 | **Custom Property Widgets** | Enhance properties pane with custom widgets (containment references, radios, list items, custom images). | `[PapyrusWebExtensionRegistry.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusWebExtensionRegistry.tsx)` `[inferred]` | F7 | `?` |
| F9 | **Custom Branding Theme** | Apply Papyrus branding themes, customized header icon, footer, and styling. | `[index.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/index.tsx)` `[inferred]` | None | `?` |

---

## §2 API / Interface Surface

This list details the GraphQL schema queries and mutations exposed by Papyrus Web.

| Operation / Entry | Handler | Purpose |
|---|---|---|
| `profileMetadatas` (Query) | `[UMLProfileMetadatasDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/UMLProfileMetadatasDataFetcher.java)` | Retrieve details for all available profiles. |
| `profileLastVersion` (Query) | `[EditingContextProfileLastVersionDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/EditingContextProfileLastVersionDataFetcher.java)` | Fetch the latest version of a profile. |
| `stereotypeMetatadas` (Query) | `[UMLEditingContextStereotypesDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/UMLEditingContextStereotypesDataFetcher.java)` | Retrieve stereotypes available for an element. |
| `applyProfile` (Mutation) | `[MutationApplyProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyProfileDataFetcher.java)` | Apply profile to a UML model. |
| `applyStereotype` (Mutation) | `[MutationApplyStereotypeDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyStereotypeDataFetcher.java)` | Apply stereotype to a UML model element. |
| `publishProfile` (Mutation) | `[MutationPublishProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationPublishProfileDataFetcher.java)` | Publish a dynamic profile diagram. |
| `deletePublishedDynamicProfileByName` (Mutation) | `[MutationDeletePublishedDynamicProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationDeletePublishedDynamicProfileDataFetcher.java)` | Delete published dynamic profile by name. |

---

## §3 Full-Stack Touch Lists

### F1 — Profiles Dashboard
| What to change | File / Component | Confidence |
|---|---|---|
| UI Dashboard View | `[ProfilesPage.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesPage.tsx)` | `[inferred]` |
| UI Edit Profile View | `[DisplayProfileView.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/DisplayProfileView.tsx)` | `[inferred]` |
| UI Menu Navigation | `[ProfilesMenuItem.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesMenuItem.tsx)` | `[inferred]` |
| Backend GQL Query | `[ProfileCurrentEditingContextDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/ProfileCurrentEditingContextDataFetcher.java)` | `[inferred]` |
| Backend Service | `[UMLProfileService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/services/UMLProfileService.java)` | `[inferred]` |
| Relational DB Entity | `[ProfileResourceEntity.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-domain/src/main/java/org/eclipse/papyrus/web/domain/boundedcontext/profile/ProfileResourceEntity.java)` | `[inferred]` |

### F2 — Apply UML Profile
| What to change | File / Component | Confidence |
|---|---|---|
| Tree Menu Contribution | `[UMLModelTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/UMLModelTreeItemContextMenuContribution.tsx)` | `[inferred]` |
| Apply Modal Component | `[ApplyProfileModal.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/ApplyProfileModal.tsx)` | `[inferred]` |
| Apply Modal State Machine | `[ApplyProfileModalMachine.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/ApplyProfileModalMachine.tsx)` | `[inferred]` |
| Backend GQL Mutation | `[MutationApplyProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyProfileDataFetcher.java)` | `[inferred]` |
| Backend Event Handler | `[ApplyProfileEventHandler.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/handlers/ApplyProfileEventHandler.java)` | `[inferred]` |

### F3 — Apply UML Stereotype
| What to change | File / Component | Confidence |
|---|---|---|
| Element Tree Menu Entry | `[UMLElementTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/UMLElementTreeItemContextMenuContribution.tsx)` | `[inferred]` |
| Stereotype Apply Modal | `[ApplyStereotypeModal.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/ApplyStereotypeModal.tsx)` | `[inferred]` |
| State Machine | `[ApplyStereotypeModalMachine.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/ApplyStereotypeModalMachine.ts)` | `[inferred]` |
| Backend GQL Mutation | `[MutationApplyStereotypeDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyStereotypeDataFetcher.java)` | `[inferred]` |
| Backend Service | `[UMLStereotypeService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/services/UMLStereotypeService.java)` | `[inferred]` |

### F4 — Publish UML Profile
| What to change | File / Component | Confidence |
|---|---|---|
| Profile Tree Menu Entry | `[PublishProfileTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/publish-profile/PublishProfileTreeItemContextMenuContribution.tsx)` | `[inferred]` |
| Publish Dialog | `[PublishProfileDialog.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/publish-profile/PublishProfileDialog.tsx)` | `[inferred]` |
| Backend GQL Mutation | `[MutationPublishProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationPublishProfileDataFetcher.java)` | `[inferred]` |
| Backend Event Handler | `[PublishProfileEventHandler.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/handlers/PublishProfileEventHandler.java)` | `[inferred]` |

### F5 — Import UML Library
| What to change | File / Component | Confidence |
|---|---|---|
| Frontend Command Seam | `[PapyrusWebExtensionRegistry.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusWebExtensionRegistry.tsx)` | `[inferred]` |
| Backend Command Provider | `[UMLImportLibraryCommandProvider.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLImportLibraryCommandProvider.java)` | `[inferred]` |
| Resource Loader Service | `[PapyrusResourceService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusResourceService.java)` | `[inferred]` |

### F6 — Publish UML Library
| What to change | File / Component | Confidence |
|---|---|---|
| UI Command Component | `[PublishUMLLibraryCommand.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/libraries/PublishUMLLibraryCommand.tsx)` | `[inferred]` |
| UI Command Definition | `[PublishUMLLibraryCommand.types.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/libraries/PublishUMLLibraryCommand.types.tsx)` | `[inferred]` |
| Backend Command Provider | `[UMLLibraryPublicationCommandProvider.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLLibraryPublicationCommandProvider.java)` | `[inferred]` |
| Backend Publisher Service | `[PapyrusUMLLibraryPublisher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusUMLLibraryPublisher.java)` | `[inferred]` |
| Backend Event Handler | `[UMLLibraryPublicationHandler.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLLibraryPublicationHandler.java)` | `[inferred]` |

### F7 — Custom Diagram Shapes (Nodes)
| What to change | File / Component | Confidence |
|---|---|---|
| Node Registry | `[PapyrusNodeTypeRegistryValue.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusNodeTypeRegistryValue.tsx)` | `[inferred]` |
| Node UI Component | `[PackageNode.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/nodes/package/PackageNode.tsx)` | `[inferred]` |
| Node GraphQL Transform | `[NodesDocumentTransform.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/nodes/NodesDocumentTransform.ts)` | `[inferred]` |
| Custom Node GraphQL Schema | `[customnodes.graphqls](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/resources/schema/customnodes.graphqls)` | `[inferred]` |

### F8 — Custom Property Widgets
| What to change | File / Component | Confidence |
|---|---|---|
| Widget Extensions Registry | `[PapyrusWebExtensionRegistry.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusWebExtensionRegistry.tsx)` | `[inferred]` |
| Custom Widget UI | `[CustomImageSection.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/widgets/customImage/CustomImageSection.tsx)` | `[inferred]` |
| Widget GraphQL Transform | `[CustomWidgetsDocumentTransform.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/widgets/CustomWidgetsDocumentTransform.ts)` | `[inferred]` |
| Custom Image GraphQL Schema | `[custom-image.graphqls](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-custom-widgets/src/main/resources/schema/custom-image.graphqls)` | `[inferred]` |

### F9 — Custom Branding Theme
| What to change | File / Component | Confidence |
|---|---|---|
| App Customization Entry | `[index.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/index.tsx)` | `[inferred]` |
| Material-UI Theme Config | `[papyrusTheme.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/theme/papyrusTheme.ts)` | `[inferred]` |
| Navigation Header Icon | `[PapyrusNavigationBarIcon.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/core/PapyrusNavigationBarIcon.tsx)` | `[inferred]` |
| Footer UI Component | `[Footer.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/footer/Footer.tsx)` | `[inferred]` |

---

## §4 Where New Code Lives — decision tree

```
What kind of change?
├── New custom UI element or styling?
│   ├── Main SPA layout & branding   ➔ frontend/papyrus-web/src/
│   └── Shared component / context   ➔ frontend/papyrus-web-components/src/
├── New GraphQL query or mutation?
│   ├── Frontend query / schema       ➔ frontend/papyrus-web-components/src/registry/
│   ├── Backend GraphQL controller    ➔ backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/.../controllers/
│   └── Backend GraphQL schema definitions ➔ backend/papyrus-web-application/src/main/resources/schema/
├── Custom Diagram Shapes / Nodes?
│   ├── Frontend ReactFlow component  ➔ frontend/papyrus-web-components/src/nodes/
│   └── Backend shape builder logic   ➔ backend/papyrus-web-representation-builder/
├── Custom Property Form Widgets?
│   ├── Frontend MUI widget code      ➔ frontend/papyrus-web-components/src/widgets/
│   └── Backend widget definition     ➔ backend/papyrus-web-custom-widgets/
└── New backend UML core logic / profiles?
    ├── Profiles backend logic        ➔ backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/
    ├── Database Entity & Schema      ➔ backend/papyrus-web-domain/ or backend/papyrus-web-infra/src/main/resources/db/
    └── C++/Java/CodeGen UML Profiles ➔ backend/papyrus-web-cpp-profile/, backend/papyrus-web-java-profile/, etc.
```

---

## §5 Specification-Driven Development — new features

New features follow a spec-first workflow:

```
1. Create specification: ai/lab/specs/SPEC_<feature-name>.md
2. AI fills in: Full-stack design based on this catalog
3. Human approves spec
4. AI implements using the /add-feature skill
5. AI updates: FEATURE_CATALOG.md
```

---

## §6 The 3-File Rule

For quick onboarding, read these three files first to understand each feature area:

- **Profiles Dashboard (F1)**
  1. [ProfilesPage.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/profiles/ProfilesPage.tsx)
  2. [UMLProfileService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/services/UMLProfileService.java)
  3. [ProfileResourceEntity.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-domain/src/main/java/org/eclipse/papyrus/web/domain/boundedcontext/profile/ProfileResourceEntity.java)

- **Apply UML Profile (F2)**
  1. [UMLModelTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/UMLModelTreeItemContextMenuContribution.tsx)
  2. [ApplyProfileModal.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-profile/ApplyProfileModal.tsx)
  3. [MutationApplyProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyProfileDataFetcher.java)

- **Apply UML Stereotype (F3)**
  1. [UMLElementTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/UMLElementTreeItemContextMenuContribution.tsx)
  2. [ApplyStereotypeModal.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/apply-stereotype/ApplyStereotypeModal.tsx)
  3. [MutationApplyStereotypeDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationApplyStereotypeDataFetcher.java)

- **Publish UML Profile (F4)**
  1. [PublishProfileTreeItemContextMenuContribution.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/publish-profile/PublishProfileTreeItemContextMenuContribution.tsx)
  2. [PublishProfileDialog.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/profile/publish-profile/PublishProfileDialog.tsx)
  3. [MutationPublishProfileDataFetcher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/profile/controllers/MutationPublishProfileDataFetcher.java)

- **Import UML Library (F5)**
  1. [PapyrusWebExtensionRegistry.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusWebExtensionRegistry.tsx)
  2. [UMLImportLibraryCommandProvider.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLImportLibraryCommandProvider.java)
  3. [PapyrusResourceService.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusResourceService.java)

- **Publish UML Library (F6)**
  1. [PublishUMLLibraryCommand.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/libraries/PublishUMLLibraryCommand.tsx)
  2. [PapyrusUMLLibraryPublisher.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/PapyrusUMLLibraryPublisher.java)
  3. [UMLLibraryPublicationHandler.java](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/java/org/eclipse/papyrus/web/application/uml/services/library/UMLLibraryPublicationHandler.java)

- **Custom Diagram Shapes (Nodes) (F7)**
  1. [PapyrusNodeTypeRegistryValue.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusNodeTypeRegistryValue.tsx)
  2. [customnodes.graphqls](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-application/src/main/resources/schema/customnodes.graphqls)
  3. [NodesDocumentTransform.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/nodes/NodesDocumentTransform.ts)

- **Custom Property Widgets (F8)**
  1. [PapyrusWebExtensionRegistry.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/registry/PapyrusWebExtensionRegistry.tsx)
  2. [CustomWidgetsDocumentTransform.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web-components/src/widgets/CustomWidgetsDocumentTransform.ts)
  3. [custom-image.graphqls](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/backend/papyrus-web-custom-widgets/src/main/resources/schema/custom-image.graphqls)

- **Custom Branding Theme (F9)**
  1. [index.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/index.tsx)
  2. [papyrusTheme.ts](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/theme/papyrusTheme.ts)
  3. [Footer.tsx](file:///c:/Users/ks248120/Documents/GitHub/papyrus-web-plus-plus/frontend/papyrus-web/src/footer/Footer.tsx)

---

## §7 Human Spot-Check Sampling Guide

Humans should spot-check these 5 catalog definitions to ensure accuracy of implementation details:
1. Touch lists for **Publish UML Library (F6)**, specifically `PapyrusUMLLibraryPublisher.java` and `UMLLibraryPublicationHandler.java`.
2. Schema details for **Custom Diagram Shapes (Nodes) (F7)**, specifically `customnodes.graphqls`.
3. UI structure for **Apply UML Profile (F2)**, specifically `ApplyProfileModal.tsx` and XState machine `ApplyProfileModalMachine.tsx`.
4. Persistence for **Profiles Dashboard (F1)** in the custom database table `profile` via `ProfileResourceEntity.java`.
5. GraphQL Mutation signature mapping for **Apply UML Stereotype (F3)** in `MutationApplyStereotypeDataFetcher.java`.
