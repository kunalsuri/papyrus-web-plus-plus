/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.application.explorer.builder.aqlservices;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.explorer.ImportedElementTreeItem;
import org.eclipse.papyrus.web.application.explorer.PapyrusTreeFilterProvider;
import org.eclipse.papyrus.web.application.readonly.services.api.IPapyrusReadOnlyChecker;
import org.eclipse.sirius.components.collaborative.api.IRepresentationImageProvider;
import org.eclipse.sirius.components.core.CoreImageConstants;
import org.eclipse.sirius.components.core.api.IDefaultObjectSearchService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.domain.NamedElement;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerDescriptionProvider;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.Stereotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Services used in the UML default explorer Tree descriptions.
 *
 * @author Arthur Daussy
 */
public class UMLDefaultTreeServices {

    /**
     * Color used for read only element.
     */
    public static final String READ_ONLY_COLOR = "#888888";

    /**
     * Color used for stereotype applications.
     */
    public static final String STEREOTYPE_APPLICATION_COLOR = "#98bf0c";

    /**
     * ID prefix used for {@link ImportedElementTreeItem}.
     */
    // Shorten id as a workaround for bug https://github.com/eclipse-sirius/sirius-web/issues/4081
    public static final String PAPYRUS_IMPORTED_ELEMENT_PREFIX = "p://ie";

    /**
     * Id of the semantic element imported.
     */
    // Shorten id has a workaround for bug https://github.com/eclipse-sirius/sirius-web/issues/4081
    public static final String IMPORTED_ELEMENT_ID = "el";

    /**
     * Tree Item id of a {@link ImportedElementTreeItem}
     */
    // Shorten id has a workaround for bug https://github.com/eclipse-sirius/sirius-web/issues/4081
    private static final String ITEM_ID = "id";

    // Shorten id has a workaround for bug https://github.com/eclipse-sirius/sirius-web/issues/4081
    private static final String PARENT_ID = "pa";

    private static final Logger LOGGER = LoggerFactory.getLogger(UMLDefaultTreeServices.class);

    private final List<IRepresentationImageProvider> representationImageProviders;

    private final IObjectService objectService;

    private final IPapyrusReadOnlyChecker readOnlyChecker;

    private final IURLParser urlParser;

    private final IDefaultObjectSearchService defaultObjectSearchService;

    public UMLDefaultTreeServices(List<IRepresentationImageProvider> representationImageProviders, IObjectService objectService,
            IPapyrusReadOnlyChecker readOnlyChecker, IURLParser urlParser, IDefaultObjectSearchService defaultObjectSearchService) {
        this.representationImageProviders = Objects.requireNonNull(representationImageProviders);
        this.objectService = Objects.requireNonNull(objectService);
        this.readOnlyChecker = Objects.requireNonNull(readOnlyChecker);
        this.urlParser = Objects.requireNonNull(urlParser);
        this.defaultObjectSearchService = Objects.requireNonNull(defaultObjectSearchService);
    }

    /**
     * Gets the list of children for the given item element.
     *
     * @param self
     *            the item element
     * @param editingContext
     *            the current {@link IEditingContext}
     * @param expandedIds
     *            the list of expanded elements
     * @param existingRepresentations
     *            the list of all currently existing representation in the editing context
     * @return the children
     */
    public List<Object> getChildrenItems(Object self, IEditingContext editingContext, List<String> expandedIds, List<String> ancestorsIds, int index,
            List<RepresentationMetadata> existingRepresentations) {
        List<Object> result = new ArrayList<>();

        if (editingContext != null) {
            String id = this.getItemId(self);
            if (expandedIds.contains(id)) {
                if (self instanceof Resource resource) {
                    result.addAll(this.filterNonUMLElement(resource.getContents()));
                } else if (self instanceof Element element) {
                    result.addAll(this.getElementDefaultChildren(element, id, editingContext, expandedIds, ancestorsIds, index, existingRepresentations));
                } else if (self instanceof ImportedElementTreeItem importTreeItem) {
                    this.getElementDefaultChildren(importTreeItem.importedElement(), id, editingContext, expandedIds, ancestorsIds, index, existingRepresentations).stream()
                            .map(c -> this.wrapToImportedElement(c, importTreeItem, ancestorsIds, index))
                            .filter(Objects::nonNull)
                            .forEach(result::add);
                }
            }
        }
        return result;
    }

    /**
     * Checks if the given element is a {@link ImportedElementTreeItem}.
     *
     * @param o
     *            an object
     * @return <code>true</code> if is a {@link ImportedElementTreeItem}
     */
    public boolean isImportedElementItem(Object o) {
        return o instanceof ImportedElementTreeItem;
    }

    /**
     * Gets the label used in the "StereotypeApplication" part of a label.
     *
     * @param input
     *            a TreeItem
     * @return a label
     */
    public String getAppliedStereotypesLabel(Object input) {
        String label = "";
        if (input instanceof ImportedElementTreeItem item) {
            label = this.getAppliedStereotypesLabel(item.importedElement());
        } else if (input instanceof Element element) {
            EList<Stereotype> appliedStereotypes = element.getAppliedStereotypes();
            if (!appliedStereotypes.isEmpty()) {
                label = appliedStereotypes.stream().map(Stereotype::getName).collect(joining(", ", UMLCharacters.ST_LEFT, UMLCharacters.ST_RIGHT + " "));
            }
        }
        return label;
    }

    private Object wrapToImportedElement(Object c, ImportedElementTreeItem parent, List<String> ancestorsIds, int index) {
        final Object result;
        if (c instanceof Element childElement) {
            result = new ImportedElementTreeItem(childElement, parent.itemId(), this.computeUniqueId(ancestorsIds, index));
        } else if (c instanceof ImportedElementTreeItem childImportedElement) {
            result = new ImportedElementTreeItem(childImportedElement.importedElement(), parent.itemId(), this.computeUniqueId(ancestorsIds, index));
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Gets the default children for a basic {@link Element}.
     *
     * @param element
     *            the current element
     * @param elementId
     *            the id of the current element
     * @param editingContext
     *            the current {@link IEditingContext}
     * @param expandedIds
     *            the list of expanded elements
     * @param index
     *            index of item in its parent list
     * @param ancestorsIds
     *            list of all ancestor ids
     * @return the children
     */
    private List<Object> getElementDefaultChildren(Element element, String elementId, IEditingContext editingContext, List<String> expandedIds, List<String> ancestorsIds, int index,
            List<RepresentationMetadata> existingRepresentations) {
        List<Object> result = new ArrayList<>();

        result.addAll(this.getRepresentations(elementId, editingContext, existingRepresentations));
        result.addAll(this.getSemanticChildren(element));
        result.addAll(this.getComputedChildren(element, ancestorsIds, index));

        return result;

    }

    private List<Object> getComputedChildren(Element element, List<String> ancestorsIds, int index) {
        final List<Object> result;
        if (element instanceof PackageImport pImport && pImport.getImportedPackage() != null) {
            result = List.of(new ImportedElementTreeItem(pImport.getImportedPackage(), this.objectService.getId(pImport), this.computeUniqueId(ancestorsIds, index)));
        } else if (element instanceof ElementImport eImport && eImport.getImportedElement() != null) {
            result = List.of(new ImportedElementTreeItem(eImport.getImportedElement(), this.objectService.getId(eImport), this.computeUniqueId(ancestorsIds, index)));
        } else {
            result = List.of();
        }

        return result;
    }

    private List<Object> getSemanticChildren(Element element) {
        return this.filterNonUMLElement(this.objectService.getContents(element));
    }

    private String computeUniqueId(List<String> ancestorIds, int index) {
        return UUID.nameUUIDFromBytes((String.join("::", ancestorIds) + "#" + index).getBytes()).toString();
    }

    private List<Object> filterNonUMLElement(List<?> input) {
        // The main purpose of this filter is to remove
        // * EAnnotations
        // * StereotypeApplications
        return input.stream().filter(Element.class::isInstance).map(Object.class::cast).toList();
    }

    /**
     * Get all representations linked to the given semantic element id
     *
     * @param elementId
     *            the id
     * @param editingContext
     *            the current {@link IEditingContext}
     * @return a list of representation
     */
    private List<RepresentationMetadata> getRepresentations(String elementId, IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations) {
        return this.findRepresentationsForTargetObjectId(existingRepresentations, elementId)
                .sorted(Comparator.comparing(RepresentationMetadata::getLabel))
                .toList();
    }

    /**
     * Gets the root elements of the tree.
     *
     * @param self
     *            the context
     * @param activeFilterIds
     *            the active filters
     * @return a list of resources
     */
    public List<Resource> getRootElements(Object self, List<String> activeFilterIds) {
        if (self instanceof EditingContext emfWebContext) {
            boolean excludeRealOnlyResources = activeFilterIds.contains(PapyrusTreeFilterProvider.HIDE_PATHMAP_URI_TREE_ITEM_FILTER_ID);
            return emfWebContext.getDomain().getResourceSet().getResources().stream()
                    .filter(r -> !excludeRealOnlyResources || !this.readOnlyChecker.isReadOnly(r))
                    .sorted(Comparator.nullsLast(Comparator.comparing(this::getResourceLabel, String.CASE_INSENSITIVE_ORDER)))
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Gets the kind of the given tree item.
     *
     * @param self
     *            the current tree item
     * @return the kind of this element
     */
    public String getItemKind(Object self) {
        String kind = "";
        if (self instanceof RepresentationMetadata representationMetadata) {
            kind = representationMetadata.getKind();
        } else if (self instanceof Resource) {
            kind = ExplorerDescriptionProvider.DOCUMENT_KIND;
        } else if (self instanceof ImportedElementTreeItem) {
            kind = "ImportedElement";
        } else {
            kind = this.objectService.getKind(self);
        }
        return kind;
    }

    /**
     * Check if the given item has at least one child.
     *
     * @param self
     *            the current item
     * @param editingContext
     *            the current {@link IEditingContext}
     * @return <code>true</code> if it has at least one children
     */
    public boolean hasChildren(Object self, EditingContext editingContext, List<String> ancestorsIds, int index, List<RepresentationMetadata> existingRepresentations) {
        boolean hasChildren = false;
        if (self instanceof Resource resource) {
            hasChildren = !resource.getContents().isEmpty();
        } else if (self instanceof Element element) {
            hasChildren = !this.getSemanticChildren(element).isEmpty()
                    || this.hasRepresentation(editingContext, element, existingRepresentations)
                    || !this.getComputedChildren(element, ancestorsIds, index).isEmpty();
        } else if (self instanceof ImportedElementTreeItem importElement) {
            hasChildren = this.hasChildren(importElement.importedElement(), editingContext, ancestorsIds, index, existingRepresentations);
        }
        return hasChildren;
    }

    private Stream<RepresentationMetadata> findRepresentationsForTargetObjectId(List<RepresentationMetadata> existingRepresentations, String targetObjectd) {
        return existingRepresentations.stream().filter(representationMetadata -> representationMetadata.getTargetObjectId().equals(targetObjectd));
    }

    private boolean hasRepresentation(EditingContext editingContext, EObject self, List<RepresentationMetadata> existingRepresentations) {
        boolean hasChildren = false;
        var optionalEditingContextId = new UUIDParser().parse(editingContext.getId());
        if (optionalEditingContextId.isPresent()) {
            String id = this.objectService.getId(self);
            hasChildren = this.findRepresentationsForTargetObjectId(existingRepresentations, id).findAny().isPresent();
        }
        return hasChildren;
    }

    /**
     * Compute the id of the given element.
     *
     * @param self
     *            the current tree item
     * @return an id
     */
    public String getItemId(Object self) {
        String id = null;
        if (self instanceof RepresentationMetadata representationMetadata) {
            id = Objects.requireNonNull(representationMetadata.getId()).toString();
        } else if (self instanceof Resource resource) {
            id = resource.getURI().path().substring(1);
        } else if (self instanceof EObject) {
            id = this.objectService.getId(self);
        } else if (self instanceof ImportedElementTreeItem importElement) {
            id = this.getImporterElementId(importElement);
        }
        return id;
    }

    private String getImporterElementId(ImportedElementTreeItem importElement) {
        return UriComponentsBuilder.fromUriString(PAPYRUS_IMPORTED_ELEMENT_PREFIX)
                .queryParam(IMPORTED_ELEMENT_ID, this.objectService.getId(importElement.importedElement()))
                .queryParam(PARENT_ID, importElement.parentElementId())
                .queryParam(ITEM_ID, importElement.itemId())
                .encode()
                .build().toUri().toString();

    }

    /**
     * Gets the label of a tree item.
     *
     * @param self
     *            a tree item
     * @return a label
     */
    public String getItemLabel(Object self) {
        String label = "";

        if (self instanceof RepresentationMetadata representationMetadata) {
            label = representationMetadata.getLabel();
        } else if (self instanceof Resource resource) {
            label = this.getResourceLabel(resource);
        } else if (self instanceof Element element) {
            label = this.getElementLabel(element);
        } else if (self instanceof ImportedElementTreeItem importedElement) {
            label = this.getElementLabel(importedElement.importedElement());
        } else if (self instanceof EObject) {
            label = this.objectService.getLabel(self);
        }
        return label;
    }

    /**
     * Handle the label computation for UML elements.
     *
     * @param element
     *            an UML element
     * @return a label
     */
    private String getElementLabel(Element element) {
        String mainLabel;

        if (element instanceof ProfileApplication pApplication) {
            mainLabel = "<Profile Application> " + this.objectService.getLabel(pApplication.getAppliedProfile());
        } else if (element instanceof PackageImport packageImport) {
            mainLabel = "<Package Import> " + this.objectService.getLabel(packageImport.getImportedPackage());
        } else if (element instanceof ElementImport elementImport) {
            mainLabel = "<Element Import> " + this.objectService.getLabel(elementImport.getImportedElement());
        } else {
            mainLabel = this.objectService.getLabel(element);
            // Fallback for elements which are not NamedElement and not properly handled by the Edit framework
            if ((mainLabel == null || mainLabel.isEmpty()) && !(element instanceof NamedElement)) {
                mainLabel = "<" + splitCamelCase(element.eClass().getName() + ">");
            }
        }

        return mainLabel;
    }

    private static String splitCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(input.charAt(0));

        for (int i = 1; i < input.length(); i++) {
            if (Character.isUpperCase(input.charAt(i))) {
                result.append(' ');
            }
            result.append(input.charAt(i));
        }

        return result.toString();
    }

    /**
     * Returns <code>true</code> if the {@link Element} or the {@link ImportedElementTreeItem#importedElement()} has a
     * 'isStatic' feature that return a <code>true</code> value.
     *
     * @param input
     *            a {@link TreeItem}
     * @return <code>true</code> if the element is static
     */
    public boolean isStatic(Object input) {
        boolean isStatic = false;
        if (input instanceof ImportedElementTreeItem item) {
            isStatic = this.isStatic(item.importedElement());
        } else if (input instanceof Element element) {
            EStructuralFeature staticFeature = element.eClass().getEStructuralFeature("isStatic");
            if (staticFeature != null) {
                isStatic = (boolean) element.eGet(staticFeature);
            }
        }
        return isStatic;
    }

    /**
     * Returns <code>true</code> if the {@link Element} or the {@link ImportedElementTreeItem#importedElement()} has a
     * 'isAbstract' feature that return a <code>true</code> value.
     *
     * @param input
     *            a {@link TreeItem}
     * @return <code>true</code> if the element is static
     */
    public boolean isAbstract(Object input) {
        boolean isAbstract = false;
        if (input instanceof ImportedElementTreeItem item) {
            isAbstract = this.isStatic(item.importedElement());
        } else if (input instanceof Element element) {
            EStructuralFeature staticFeature = element.eClass().getEStructuralFeature("isAbstract");
            if (staticFeature != null) {
                isAbstract = (boolean) element.eGet(staticFeature);
            }
        }
        return isAbstract;
    }

    /**
     * Gets the color to be used for the main part of the label.
     *
     * @param element
     *            an element
     * @return a color or <code>null</code> to use the default one
     */
    public String getMainLabelColor(Object element) {
        if (element instanceof ImportedElementTreeItem || this.readOnlyChecker.isReadOnly(element)) {
            return READ_ONLY_COLOR;
        } else {
            return null;
        }

    }

    /**
     * Gets the color to be used in the "stereotype application" part of the label.
     *
     * @param element
     *            an element
     * @return a color
     */
    public String getStereotypeApplicationLabelColor(Object element) {
        if (element instanceof ImportedElementTreeItem || this.readOnlyChecker.isReadOnly(element)) {
            return READ_ONLY_COLOR;
        } else {
            return STEREOTYPE_APPLICATION_COLOR;
        }

    }

    /**
     * Gets a object from its id.
     *
     * @param treeItemId
     *            an id
     * @param editingContext
     *            the current editing context
     * @return an object or <code>null</code>
     */
    public Object toObject(String treeItemId, IEditingContext editingContext) {
        Object result = null;
        // Handle here the importedElements
        if (treeItemId != null && treeItemId.startsWith(PAPYRUS_IMPORTED_ELEMENT_PREFIX)) {
            result = this.fromId(editingContext, treeItemId);
        } else {
            // Fast path to avoid potentially costly IObjectSearchServiceDelegates
            var optionalObject = this.defaultObjectSearchService.getObject(editingContext, treeItemId);
            if (optionalObject.isEmpty()) {
                // Slow path: fallback to the full algorithm
                optionalObject = this.objectService.getObject(editingContext, treeItemId);
            }

            if (optionalObject.isPresent()) {
                result = optionalObject.get();
            } else {
                var optionalEditingDomain = Optional.of(editingContext)
                        .filter(IEMFEditingContext.class::isInstance)
                        .map(IEMFEditingContext.class::cast)
                        .map(IEMFEditingContext::getDomain);

                if (optionalEditingDomain.isPresent()) {
                    var editingDomain = optionalEditingDomain.get();
                    ResourceSet resourceSet = editingDomain.getResourceSet();
                    URI uri = new JSONResourceFactory().createResourceURI(treeItemId);

                    result = resourceSet.getResources().stream()
                            .filter(resource -> resource.getURI().equals(uri))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
        return result;
    }

    /**
     * Creates a new {@link ImportedElementTreeItem} from a given id
     *
     * @param editingContext
     *            the current {@link IEditingContext}
     * @param itemId
     *            the id of the tree item
     * @return the imported element tree item corresponding to given tree item
     */
    private ImportedElementTreeItem fromId(IEditingContext editingContext, String itemId) {
        ImportedElementTreeItem result = null;
        try {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(itemId);
            if (parameters != null) {
                List<String> parentIds = parameters.get(PARENT_ID);
                Optional<Element> aImportedElement = this.toElement(parameters, editingContext);

                List<String> importItemIts = parameters.get(ITEM_ID);

                if (this.isNonEmpty(parentIds) && aImportedElement.isPresent() && this.isNonEmpty(importItemIts)) {
                    result = new ImportedElementTreeItem(aImportedElement.get(), parentIds.get(0), importItemIts.get(0));
                }
            }

        } catch (IllegalStateException e) {
            LOGGER.warn("Invalid importedElement id {} : {}", itemId, e.getCause());
        }

        if (result == null) {
            LOGGER.warn("Invalid importedElement id {}", itemId);
        }

        return result;

    }

    private Optional<Element> toElement(Map<String, List<String>> parameters, IEditingContext editingContext) {
        List<String> id = parameters.get(UMLDefaultTreeServices.IMPORTED_ELEMENT_ID);
        if (this.isNonEmpty(id)) {
            return this.objectService.getObject(editingContext, id.get(0))
                    .filter(Element.class::isInstance)
                    .map(Element.class::cast);
        }
        return Optional.empty();
    }

    private boolean isNonEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Gets the list of all icons to be applied on tree item.
     *
     * @param self
     *            the tree item
     * @return a list of icons URL
     */
    public List<String> getIconURLs(Object self) {
        List<String> imageURL = List.of(CoreImageConstants.DEFAULT_SVG);
        if (self instanceof EObject) {
            imageURL = this.objectService.getImagePath(self);
        } else if (self instanceof RepresentationMetadata representationMetadata) {
            imageURL = this.representationImageProviders.stream()
                    .map(representationImageProvider -> representationImageProvider.getImageURL(representationMetadata.getKind()))
                    .flatMap(Optional::stream)
                    .toList();
        } else if (self instanceof Resource) {
            imageURL = List.of("/explorer/Resource.svg");
        } else if (self instanceof ImportedElementTreeItem importedElement) {
            imageURL = this.objectService.getImagePath(importedElement.importedElement());
        }
        return imageURL;
    }

    /**
     * Checks if an element can be deleted.
     *
     * @param self
     *            the tree item
     * @return true if the element can be deleted
     */
    public boolean canBeDeleted(Object self) {
        return !this.readOnlyChecker.isReadOnly(self) && !(self instanceof ImportedElementTreeItem);
    }

    /**
     * Checks if an element can be renamed.
     *
     * @param self
     *            the tree item
     * @return true if the element can be renamed
     */
    public boolean canBeRenamed(Object self) {
        return !this.readOnlyChecker.isReadOnly(self) && !(self instanceof ImportedElementTreeItem);
    }

    /**
     * Gets the parent item of the given item.
     *
     * @param self
     *            the current item
     * @param treeItemId
     *            the id of the current item
     * @param editingContext
     *            the editing context
     * @return the parent item or null
     */
    public Object getParentItem(Object self, String treeItemId, IEditingContext editingContext) {
        Object result = null;

        if (self instanceof RepresentationMetadata representationMetadata) {
            result = this.objectService.getObject(editingContext, representationMetadata.getTargetObjectId());
        } else if (self instanceof EObject eObject) {
            Object semanticContainer = eObject.eContainer();
            if (semanticContainer == null) {
                semanticContainer = eObject.eResource();
            }
            result = semanticContainer;
        } else if (self instanceof ImportedElementTreeItem importedItem) {
            String parentId = importedItem.parentElementId();
            result = this.objectService.getObject(editingContext, parentId).orElse(null);
            // For the moment we only handle imported elements below the PackageImport or the ElementImport
            // Nested imported element can't return a parent here because we can't build the parent
            // ImportedElementTreeItem only from the parentId
            // We can't store the complete id here because if we do the size of the id will grow when we go deeper and
            // deeper
        }
        return result;
    }

    private String getResourceLabel(Resource resource) {
        return resource.eAdapters().stream()
                .filter(ResourceMetadataAdapter.class::isInstance)
                .map(ResourceMetadataAdapter.class::cast)
                .findFirst()
                .map(ResourceMetadataAdapter::getName)
                .orElse(resource.getURI().lastSegment());
    }
}
