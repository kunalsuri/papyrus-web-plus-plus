/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.uml.domain.services.profile.StereotypeUtil;
import org.eclipse.sirius.components.collaborative.browser.ModelBrowserDefaultCandidateSearchProvider;
import org.eclipse.sirius.components.collaborative.browser.ModelBrowserDescriptionProvider;
import org.eclipse.sirius.components.collaborative.browser.api.IModelBrowserRootCandidateSearchProvider;
import org.eclipse.sirius.components.core.CoreImageConstants;
import org.eclipse.sirius.components.core.URLParser;
import org.eclipse.sirius.components.core.api.IContentService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.core.api.SemanticKindConstants;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.trees.renderer.TreeRenderer;
import org.eclipse.uml2.uml.Element;
import org.springframework.stereotype.Service;

/**
 * Override default representation
 * {@link org.eclipse.sirius.components.collaborative.widget.reference.browser.ModelBrowsersDescriptionProvider#REFERENCE_DESCRIPTION_ID}.
 *
 * <p>
 * This interface has been created for bug
 * https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web/-/issues/97. But once
 * https://github.com/eclipse-sirius/sirius-web/issues/2809 is fixed this is no longer needed
 * </p>
 *
 * @author Arthur Daussy
 */
// Most of this call has been copied from
// org.eclipse.sirius.components.collaborative.widget.reference.browser.ModelBrowsersDescriptionProvider
// With the difference that we did not remove the use isSelectable filter to compute the list of children
@Service
public class ReferenceModelBrowerDescriptionOverrider implements IRepresentationDescriptionOverrider {

    public static final String REPRESENTATION_NAME = "Papyrus Reference Model Browser";

    public static final String DOCUMENT_KIND = "siriusWeb://document";

    public static final String TREE_KIND = "modelBrowser://";

    public static final String MODEL_BROWSER_REFERENCE_PREFIX = "modelBrowser://reference";

    private static final String TARGET_TYPE = "targetType";

    private final ILabelService labelService;
    private final IIdentityService identityService;
    private final IContentService contentService;
    private final IObjectSearchService objectSearchService;

    private final ModelBrowserDefaultCandidateSearchProvider defaultCandidateProvider;

    private final IEMFKindService emfKindService;

    private final List<IModelBrowserRootCandidateSearchProvider> candidateProviders;

    private ModelBrowserDescriptionProvider modelBrowserDescriptionProvider;

    private IURLParser urlParser = new URLParser();

    public ReferenceModelBrowerDescriptionOverrider(ILabelService labelService, IEMFKindService emfKindService, List<IModelBrowserRootCandidateSearchProvider> candidateProviders,
            ModelBrowserDescriptionProvider modelBrowserDescriptionProvider, IIdentityService identityService, IContentService contentService, IObjectSearchService objectSearchService) {
        super();
        this.labelService = Objects.requireNonNull(labelService);
        this.identityService = Objects.requireNonNull(identityService);
        this.contentService = Objects.requireNonNull(contentService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.defaultCandidateProvider = new ModelBrowserDefaultCandidateSearchProvider();
        this.emfKindService = emfKindService;
        this.candidateProviders = candidateProviders;
        this.modelBrowserDescriptionProvider = Objects.requireNonNull(modelBrowserDescriptionProvider);
    }

    @Override
    public List<IRepresentationDescription> getOverridedDescriptions() {
        TreeDescription description = this.getModelBrowserDescription(ModelBrowserDescriptionProvider.REFERENCE_DESCRIPTION_ID, variableManager -> this.canCreateModelBrowser(variableManager),
                this.browserIsSelectableProvider(), this::getSearchScopeElements, MODEL_BROWSER_REFERENCE_PREFIX);
        return List.of(description);
    }

    private TreeDescription getModelBrowserDescription(String descriptionId, Predicate<VariableManager> canCreatePredicate, Function<VariableManager, Boolean> isSelectableProvider,
            Function<VariableManager, List<?>> elementsProvider, String treeId) {

        return TreeDescription.newTreeDescription(descriptionId)
                .label(REPRESENTATION_NAME)
                .idProvider(variableManager -> variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class).orElse(treeId))
                .treeItemIdProvider(this::getTreeItemId)
                .kindProvider(this::getKind)
                .labelProvider(this::getLabel)
                .targetObjectIdProvider(variableManager -> variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class)
                        .map(IEditingContext::getId)
                        .orElse(null))
                .treeItemIconURLsProvider(this::getImageURL)
                .editableProvider(this::isEditable)
                .deletableProvider(this::isDeletable)
                .selectableProvider(isSelectableProvider)
                .elementsProvider(elementsProvider)
                .hasChildrenProvider(variableManager -> this.hasChildren(variableManager, isSelectableProvider))
                .childrenProvider(variableManager -> this.getChildren(variableManager, isSelectableProvider))
                .canCreatePredicate(canCreatePredicate)
                .deleteHandler(this::getDeleteHandler)
                .renameHandler(this::getRenameHandler)
                .treeItemObjectProvider(this::getTreeItemObject)
                .treeItemLabelProvider(this::getLabel)
                .parentObjectProvider(this::getParentObject)
                .iconURLsProvider(variableManager -> List.of())
                .build();
    }

    private Object getTreeItemObject(VariableManager variableManager) {
        Object result = null;
        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class);
        var optionalId = variableManager.get(TreeDescription.ID, String.class);
        if (optionalId.isPresent() && optionalEditingContext.isPresent()) {
            var optionalObject = this.objectSearchService.getObject(optionalEditingContext.get(), optionalId.get());
            if (optionalObject.isPresent()) {
                result = optionalObject.get();
            } else {
                var optionalEditingDomain = Optional.of(optionalEditingContext.get())
                        .filter(IEMFEditingContext.class::isInstance)
                        .map(IEMFEditingContext.class::cast)
                        .map(IEMFEditingContext::getDomain);

                if (optionalEditingDomain.isPresent()) {
                    var editingDomain = optionalEditingDomain.get();
                    ResourceSet resourceSet = editingDomain.getResourceSet();
                    URI uri = new JSONResourceFactory().createResourceURI(optionalId.get());

                    result = resourceSet.getResources().stream()
                            .filter(resource -> resource.getURI().equals(uri))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
        return result;
    }

    private boolean hasCompatibleDescendants(VariableManager variableManager, EObject eObject, boolean isDescendant, Function<VariableManager, Boolean> isSelectableProvider) {
        VariableManager childVariableManager = variableManager.createChild();
        childVariableManager.put(VariableManager.SELF, eObject);
        return isDescendant && isSelectableProvider.apply(childVariableManager)
                || eObject.eContents().stream().anyMatch(eContent -> this.hasCompatibleDescendants(childVariableManager, eContent, true, isSelectableProvider));
    }

    private Object getParentObject(VariableManager variableManager) {
        Object result = null;
        Object self = variableManager.getVariables().get(VariableManager.SELF);
        if (self instanceof EObject eObject) {
            Object semanticContainer = eObject.eContainer();
            if (semanticContainer == null) {
                semanticContainer = eObject.eResource();
            }
            result = semanticContainer;
        }
        return result;
    }

    private boolean hasChildren(VariableManager variableManager, Function<VariableManager, Boolean> isSelectableProvider) {
        Object self = variableManager.getVariables().get(VariableManager.SELF);
        boolean hasChildren = false;
        if (self instanceof Resource resource) {
            hasChildren = !resource.getContents().isEmpty();
        } else if (self instanceof EObject eObject) {
            hasChildren = !eObject.eContents().isEmpty() && this.hasCompatibleDescendants(variableManager, eObject, false, isSelectableProvider);
        }
        return hasChildren;
    }

    private List<Object> getChildren(VariableManager variableManager, Function<VariableManager, Boolean> isSelectableProvider) {
        List<Object> result = new ArrayList<>();

        List<String> expandedIds = new ArrayList<>();
        Object objects = variableManager.getVariables().get(TreeRenderer.EXPANDED);
        if (objects instanceof List<?> list) {
            expandedIds = list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }

        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class);

        if (optionalEditingContext.isPresent()) {
            String id = this.getTreeItemId(variableManager);
            if (expandedIds.contains(id)) {
                Object self = variableManager.getVariables().get(VariableManager.SELF);

                if (self instanceof Resource resource) {
                    result.addAll(resource.getContents());
                } else if (self instanceof EObject) {
                    List<Object> contents = this.contentService.getContents(self);
                    result.addAll(contents);
                }
            }
        }
        result.removeIf(object -> {
            if (object instanceof EObject eObject) {
                VariableManager childVariableManager = variableManager.createChild();
                childVariableManager.put(VariableManager.SELF, eObject);
                return !isSelectableProvider.apply(childVariableManager) && !this.hasChildren(childVariableManager, isSelectableProvider);
            } else {
                return false;
            }
        });
        return result;
    }

    private IStatus getDeleteHandler(VariableManager variableManager) {
        return new Failure("");
    }

    private IStatus getRenameHandler(VariableManager variableManager, String newLabel) {
        return new Failure("");
    }

    private String getTreeItemId(VariableManager variableManager) {
        Object self = variableManager.getVariables().get(VariableManager.SELF);
        String id = null;
        if (self instanceof Resource resource) {
            id = resource.getURI().path().substring(1);
        } else if (self instanceof EObject) {
            id = this.identityService.getId(self);
        }
        return id;
    }

    private List<String> getImageURL(VariableManager variableManager) {
        Object self = variableManager.getVariables().get(VariableManager.SELF);
        List<String> imageURL = List.of(CoreImageConstants.DEFAULT_SVG);
        if (self instanceof EObject) {
            imageURL = this.labelService.getImagePaths(self);
        } else if (self instanceof Resource) {
            imageURL = List.of("/reference-widget-images/Resource.svg");
        }
        return imageURL;
    }

    private boolean isEditable(VariableManager variableManager) {
        return false;

    }

    private boolean isDeletable(VariableManager variableManager) {
        return false;
    }

    private String getKind(VariableManager variableManager) {
        String kind;
        Object self = variableManager.getVariables().get(VariableManager.SELF);
        if (self instanceof Resource) {
            kind = DOCUMENT_KIND;
        } else {
            kind = this.identityService.getKind(self);
        }
        return kind;
    }

    private StyledString getLabel(VariableManager variableManager) {
        Object self = variableManager.getVariables().get(VariableManager.SELF);
        String label = "";
        if (self instanceof Resource resource) {
            label = this.getResourceLabel(resource);
        } else if (self instanceof EObject) {
            StyledString styledString = this.labelService.getStyledLabel(self);
            if (!styledString.toString().isBlank()) {
                return styledString;
            } else {
                var kind = this.identityService.getKind(self);
                label = this.urlParser.getParameterValues(kind).get(SemanticKindConstants.ENTITY_ARGUMENT).get(0);
            }
        }

        return StyledString.of(label);
    }

    private String getResourceLabel(Resource resource) {
        return resource.eAdapters().stream()
                .filter(ResourceMetadataAdapter.class::isInstance)
                .map(ResourceMetadataAdapter.class::cast)
                .findFirst()
                .map(ResourceMetadataAdapter::getName)
                .orElse(resource.getURI().lastSegment());
    }

    private Function<VariableManager, Boolean> browserIsSelectableProvider() {
        return variableManager -> {

            // Customization happens here. If the target type is a stereotype use a custom predicate
            if (this.isStereotypePredicate(variableManager)) {
                return this.buildStereotypePredicate(variableManager);
            } else {
                EClass targetType = this.resolveTargetType(variableManager).orElse(null);
                boolean isContainment = this.resolveIsContainment(variableManager);
                return this.isTypeSelectable(variableManager, targetType, isContainment);
            }

        };
    }

    private Boolean canCreateModelBrowser(VariableManager variableManager) {
        return variableManager.get("treeId", String.class).map(treeId -> treeId.startsWith(MODEL_BROWSER_REFERENCE_PREFIX)).orElse(false);
    }

    private boolean buildStereotypePredicate(VariableManager variableManager) {

        Predicate<Element> elementFilter = this.getStereotypeQualifiedName(variableManager);
        var optionalSelf = variableManager.get(VariableManager.SELF, EObject.class);
        if (optionalSelf.isPresent() && elementFilter != null) {
            EObject self = optionalSelf.get();
            return self instanceof Element element && elementFilter.test(element);
        }

        return false;
    }

    private Predicate<Element> getStereotypeQualifiedName(VariableManager variableManager) {
        var optionalTreeId = variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class);
        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEMFEditingContext.class);
        if (optionalTreeId.isPresent() && optionalTreeId.get().startsWith(TREE_KIND) && optionalEditingContext.isPresent()) {
            Map<String, List<String>> parameters = new URLParser().getParameterValues(optionalTreeId.get());
            String targetType = parameters.get(TARGET_TYPE).get(0);

            String ePackageName = this.emfKindService.getEPackageName(targetType);
            String eClassName = this.emfKindService.getEClassName(targetType);

            // We need this to handle case where stereotype are named with non compliant EMF EClass name (for example
            // when using a - in there name)
            return element -> this.hasStereotypeMatchingEClass(ePackageName, eClassName, element);
        } else {
            return null;
        }
    }

    private boolean hasStereotypeMatchingEClass(String ePackageName, String eClassName, Element element) {
        return element.getAppliedStereotypes().stream().anyMatch(s -> {
            EClass def = s.getDefinition();
            return def != null && def.getName().equals(eClassName) && def.getEPackage().getName().equals(ePackageName);
        });
    }

    private boolean isStereotypePredicate(VariableManager variableManager) {
        var optionalTreeId = variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class);
        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEMFEditingContext.class);
        if (optionalTreeId.isPresent() && optionalTreeId.get().startsWith(TREE_KIND) && optionalEditingContext.isPresent()) {
            Map<String, List<String>> parameters = new URLParser().getParameterValues(optionalTreeId.get());
            String kind = parameters.get(TARGET_TYPE).get(0);
            Registry ePackageRegistry = optionalEditingContext.get().getDomain().getResourceSet().getPackageRegistry();

            String ePackageName = this.emfKindService.getEPackageName(kind);
            String eClassName = this.emfKindService.getEClassName(kind);

            // Has a EReference starting with base_ and pointing a an UML element
            Optional<EClass> targetEClass = this.modelBrowserDescriptionProvider.findEPackage(ePackageRegistry, ePackageName).map(ePackage -> ePackage.getEClassifier(eClassName))
                    .filter(EClass.class::isInstance).map(EClass.class::cast);

            return targetEClass.map(StereotypeUtil::getBaseReferences).map(s -> s.count() > 0).orElse(false);
        } else {
            return false;
        }
    }

    private boolean isTypeSelectable(VariableManager variableManager, EClass targetType, boolean isContainment) {
        var optionalSelf = variableManager.get(VariableManager.SELF, EObject.class);
        if (optionalSelf.isPresent() && targetType != null) {
            return targetType.isInstance(optionalSelf.get())
                    && this.resolveOwnerEObject(variableManager).map(eObject -> !(isContainment && EcoreUtil.isAncestor(optionalSelf.get(), eObject))).orElse(true);
        } else {
            return false;
        }
    }

    private Optional<EObject> resolveOwnerEObject(VariableManager variableManager) {
        var optionalTreeId = variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class);
        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEMFEditingContext.class);
        if (optionalTreeId.isPresent() && optionalTreeId.get().startsWith(TREE_KIND) && optionalEditingContext.isPresent()) {
            Map<String, List<String>> parameters = new URLParser().getParameterValues(optionalTreeId.get());
            String ownerId = parameters.get("ownerId").get(0);

            return this.objectSearchService.getObject(optionalEditingContext.get(), ownerId).filter(EObject.class::isInstance).map(EObject.class::cast);
        } else {
            return Optional.empty();
        }
    }

    private List<? extends Object> getSearchScopeElements(VariableManager variableManager) {
        var optionalTreeId = variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class);
        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class);
        if (optionalTreeId.isPresent() && optionalTreeId.get().startsWith(TREE_KIND) && optionalEditingContext.isPresent()) {
            Map<String, List<String>> parameters = new URLParser().getParameterValues(optionalTreeId.get());
            String descriptionId = parameters.get("descriptionId").get(0);
            String ownerId = parameters.get("ownerId").get(0);
            var semanticOwner = this.objectSearchService.getObject(optionalEditingContext.get(), ownerId).get();

            return this.candidateProviders.stream().filter(provider -> provider.canHandle(descriptionId)).findFirst().orElse(this.defaultCandidateProvider).getRootElementsForReference(semanticOwner,
                    descriptionId, optionalEditingContext.get());
        }
        return Collections.emptyList();
    }

    private Optional<EClass> resolveTargetType(VariableManager variableManager) {
        var optionalTreeId = variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class);
        var optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEMFEditingContext.class);
        if (optionalTreeId.isPresent() && optionalTreeId.get().startsWith(TREE_KIND) && optionalEditingContext.isPresent()) {
            Registry ePackageRegistry = optionalEditingContext.get().getDomain().getResourceSet().getPackageRegistry();
            Map<String, List<String>> parameters = new URLParser().getParameterValues(optionalTreeId.get());
            String kind = parameters.get(TARGET_TYPE).get(0);

            String ePackageName = this.emfKindService.getEPackageName(kind);
            String eClassName = this.emfKindService.getEClassName(kind);

            return this.modelBrowserDescriptionProvider.findEPackage(ePackageRegistry, ePackageName).map(ePackage -> ePackage.getEClassifier(eClassName)).filter(EClass.class::isInstance)
                    .map(EClass.class::cast);
        } else {
            return Optional.empty();
        }
    }

    private boolean resolveIsContainment(VariableManager variableManager) {
        var optionalTreeId = variableManager.get(GetOrCreateRandomIdProvider.PREVIOUS_REPRESENTATION_ID, String.class);
        if (optionalTreeId.isPresent() && optionalTreeId.get().startsWith(TREE_KIND)) {
            Map<String, List<String>> parameters = new URLParser().getParameterValues(optionalTreeId.get());
            String isContainment = parameters.get("isContainment").get(0);
            return Boolean.parseBoolean(isContainment);
        } else {
            return false;
        }
    }

}
