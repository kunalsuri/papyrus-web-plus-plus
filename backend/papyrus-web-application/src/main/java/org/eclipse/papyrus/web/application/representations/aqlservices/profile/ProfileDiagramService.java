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
package org.eclipse.papyrus.web.application.representations.aqlservices.profile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.PapyrusRepresentationDescriptionRegistry;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher.BorderNodeStatus;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.springframework.stereotype.Service;

/**
 * Service for the "Profile" diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class ProfileDiagramService extends AbstractDiagramService {

    private final IRepresentationSearchService representationSearchService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final PapyrusRepresentationDescriptionRegistry papyrusRepresentationRegistry;

    /**
     * Logger used to report errors and warnings to the user.
     */
    private final ILogger logger;

    /**
     * Constructor.
     *
     * @param identityService
     *         the service in charge of getting the identity of an object
     * @param labelService
     *         the service in charge of getting labels and images of an object
     * @param objectSearchService
     *         the service in charge of getting an object from its id
     * @param diagramNavigationService
     *            helper that must introspect the current diagram's structure and its description
     * @param diagramOperationsService
     *            helper that must modify the current diagram, most notably create or delete views for unsynchronized
     *            elements
     * @param editableChecker
     *            Object that check if an element can be edited
     * @param viewDiagramService
     *            Service used to navigate in DiagramDescription
     * @param representationSearchService
     *            helper used to find representation
     * @param representationDescriptionSearchService
     *            helper used to find representation descriptions
     * @param papyrusRepresentationRegistry
     *            registry that keeps track of all {@link DiagramDescription}s used in Papyrus application
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    // CHECKSTYLE:OFF
    public ProfileDiagramService(IIdentityService identityService, ILabelService labelService,
            IObjectSearchService objectSearchService, IDiagramNavigationService diagramNavigationService,
            IDiagramOperationsService diagramOperationsService, IEditableChecker editableChecker,
            IViewDiagramDescriptionService viewDiagramService, IRepresentationSearchService representationSearchService, IRepresentationDescriptionSearchService representationDescriptionSearchService,
            PapyrusRepresentationDescriptionRegistry papyrusRepresentationRegistry, ILogger logger) {
        // CHECKSTYLE:ON
        super(identityService, labelService, objectSearchService, diagramNavigationService, diagramOperationsService,
                editableChecker, viewDiagramService, logger);
        this.representationSearchService = representationSearchService;
        this.representationDescriptionSearchService = representationDescriptionSearchService;
        this.papyrusRepresentationRegistry = papyrusRepresentationRegistry;
        this.logger = logger;
    }

    @Override
    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext,
                capturedNodeDescriptions);
        IWebExternalSourceToRepresentationDropBehaviorProvider dropProvider = new ProfileSemanticDropBehaviorProvider(
                editionContext, createViewHelper, this.getObjectSearchService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    @Override
    protected IWebInternalSourceToRepresentationDropBehaviorProvider buildGraphicalDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext,
                capturedNodeDescriptions);
        IWebInternalSourceToRepresentationDropBehaviorProvider dropProvider = new ProfileGraphicalDropBehaviorProvider(
                editionContext, createViewHelper, this.getObjectSearchService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    /**
     * Checks if the provided {@code representationId} is inside a Profile model.
     *
     * @param editingContext
     *            the current editing context
     * @param representationId
     *            the identifier of the representation to check
     * @return {@code true} if the {@code representationId} is inside a profile model, {@code false} otherwise
     */
    public boolean isProfileModel(IEditingContext editingContext, String representationId) {
        boolean result = false;
        if (representationId != null && !representationId.isEmpty()) {
            Optional<Diagram> optDiagram = this.representationSearchService.findById(editingContext, representationId, Diagram.class);
            if (optDiagram.isPresent()) {
                Optional<IRepresentationDescription> optDescription = this.representationDescriptionSearchService.findById(editingContext, optDiagram.get().getDescriptionId());
                EObject eObject = (EObject) this.getObjectSearchService()
                        .getObject(editingContext, optDiagram.get().getTargetObjectId()).orElse(null);
                result = optDescription.isPresent() && Objects.equals(optDescription.get().getLabel(), PRDDiagramDescriptionBuilder.PRD_REP_NAME) && this.isProfileModel(eObject);
            }
        }
        return result;
    }

    /**
     * Provides Metaclass candidates.
     *
     * @param container
     *            the current container in which looking for the Metaclass.
     * @return the Metaclass list.
     */
    public List<? extends Class> getMetaclassPRD(EObject container) {
        List<? extends Class> importedElementCandidates = List.of();
        if (container instanceof Namespace) {
            importedElementCandidates = ((Namespace) container).getElementImports().stream() //
                    .map(ei -> ei.getImportedElement()) //
                    .filter(Class.class::isInstance) //
                    .map(Class.class::cast)//
                    .filter(c -> c.isMetaclass()) //
                    .toList();
        }
        return importedElementCandidates;
    }

    /**
     * Check if the resource of a given {@link Object} is a Profile model.
     *
     * @param context
     *            context used to create diagram on
     *
     * @return <code>true</code> if the resource is a profile model, <code>false</code> otherwise.
     */
    public boolean isProfileModel(EObject context) {
        return this.isContainedInProfileResource(context);
    }

    /**
     *
     * Returns the metaclasses of the UML metamodel.
     * <p>
     * This method only returns the metaclasses from the {@code this.UML.metamodel.uml} model.The returned list is
     * sorted by alphabetical order.
     * </p>
     *
     * @param editingContext
     *            the editing context to search into
     * @return the list of metaclasses from the UML metamodel
     */
    public List<? extends Class> getMetaclasses(IEditingContext editingContext) {
        IEMFEditingContext ed = (IEMFEditingContext) editingContext;
        Resource umlMetamodelResource = ed.getDomain().getResourceSet().getResource(URI.createURI(UMLResource.UML_METAMODEL_URI), true);
        Package umlPackage = (Package) EcoreUtil.getObjectByType(umlMetamodelResource.getContents(), UMLPackage.eINSTANCE.getPackage());
        return umlPackage.getOwnedTypes().stream() //
                .filter(Class.class::isInstance) //
                .map(Class.class::cast) //
                .sorted((metaclass1, metaclass2) -> metaclass1.getName().compareTo(metaclass2.getName())) //
                .toList();
    }

    /**
     * Creates an {@link ElementImport} referencing the provided {@code metaclassId}.
     * <p>
     * The provided {@code metaclassId} must be a valid URI of a metaclass from the UML metamodel. Metaclasses stored in
     * other resources cannot be configured with this method.
     * </p>
     *
     * @param editingContext
     *            the editing context used to perform the operation
     * @param representationId
     *            the identifier of the graphical representation where the {@link ElementImport} view is created
     * @param diagramElementId
     *            the identifier of the graphical container where the {@link ElementImport} view is created
     * @param metaclassId
     *            the identifier of the UML metaclass to reference in the created {@link ElementImport}
     * @param diagramContext
     *            the graphical context
     * @return {@code true} if the {@link ElementImport} and its view are successfully created, {@code false} otherwise
     */
    public boolean createMetaclassImport(IEditingContext editingContext, String representationId, String diagramElementId, String metaclassId, IDiagramContext diagramContext) {
        boolean result = false;
        Optional<Diagram> optDiagram = this.representationSearchService.findById(editingContext, representationId, Diagram.class);
        if (optDiagram.isPresent()) {
            Diagram diagram = optDiagram.get();
            Node diagramElement = this.getDiagramElement(editingContext, diagram, diagramElementId).orElse(null);
            Optional<Profile> optProfile;
            if (diagramElement == null) {
                optProfile = this.getProfile(editingContext, diagram.getTargetObjectId());
            } else {
                optProfile = this.getProfile(editingContext, diagramElement.getTargetObjectId());
            }
            if (optProfile.isPresent()) {
                Profile profile = optProfile.get();
                Optional<Class> optMetaclass = this.getObjectSearchService().getObject(editingContext, metaclassId)
                        .map(Class.class::cast);
                ElementImport elementImport = null;
                if (optMetaclass.isPresent()) {
                    Optional<ElementImport> optionalElementImport = this.getImportElementForMetaclass(profile, optMetaclass.get());
                    if (optionalElementImport.isPresent()) {
                        elementImport = optionalElementImport.get();
                    } else {
                        // Use null as target node because ElementImport are represented as Nodes, even if they are
                        // semantically Relationships. This means that the creation tool doesn't have a target, just a
                        // container that acts as a source.
                        elementImport = (ElementImport) this.createDomainBasedEdge(profile, optMetaclass.get(), UMLPackage.eINSTANCE.getElementImport().getName(),
                                UMLPackage.eINSTANCE.getNamespace_ElementImport().getName(), diagramElement, null, editingContext, diagramContext);
                    }
                }
                if (elementImport != null) {
                    boolean isNodePresentInParent = false;
                    if (diagramElement != null) {
                        isNodePresentInParent = diagramElement.getChildNodes().stream() //
                                .anyMatch(p -> optMetaclass.get().getName().equals(p.getTargetObjectLabel()));
                    } else {
                        // The node is created on the diagram
                        isNodePresentInParent = diagramContext.getDiagram().getNodes().stream() //
                                .anyMatch(p -> optMetaclass.get().getName().equals(p.getTargetObjectLabel()));
                    }
                    if (!isNodePresentInParent) {
                        result = this.createMetaclassNode(diagramContext, diagramElement, elementImport);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Create a Metaclass node in given {@code ParentNodeQuery} representing the imported element of given
     * {@code elementImport}.
     *
     * @param diagramContext
     *            the graphical context
     * @param parentNode
     *            the parent node of the Metaclass node
     * @param elementImport
     *            the semantic {@link ElementImport} with imported element represented by the Metaclass node
     * @return {@code true} if a creation request has been made, {@code false} otherwise
     */
    private boolean createMetaclassNode(IDiagramContext diagramContext, Node parentNode, ElementImport elementImport) {
        boolean result;
        Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes = this.papyrusRepresentationRegistry
                .getConvertedNode(PRDDiagramDescriptionBuilder.PRD_REP_NAME);

        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext, convertedNodes);
        if (parentNode == null) {
            result = createViewHelper.createRootView(elementImport.getImportedElement(), PRDDiagramDescriptionBuilder.PRD_METACLASS);
        } else {
            result = createViewHelper.createChildView(elementImport.getImportedElement(), parentNode, PRDDiagramDescriptionBuilder.PRD_SHARED_METACLASS);
        }
        return result;
    }

    /**
     * Returns the semantic {@link Profile} associated to the given {@code objectId}.
     * <p>
     * This method returns an empty {@link Optional} if the provided {@code objectId} element isn't a {@link Profile}.
     * </p>
     *
     * @param editingContext
     *            the editing context
     * @param objectId
     *            the object identifier to retrieve the {@link Profile} from
     * @return the {@link Profile} if it exists, or an empty {@link Optional} otherwise
     * @throws NullPointerException
     *             if {@code editingContext} or {@code objectId} is {@code null}
     */
    private Optional<Profile> getProfile(IEditingContext editingContext, String objectId) {
        Objects.requireNonNull(editingContext);
        Objects.requireNonNull(objectId);
        return this.getObjectSearchService().getObject(editingContext, objectId)//
                .filter(Profile.class::isInstance)//
                .map(Profile.class::cast);
    }

    /**
     * Get {@link ElementImport} contained by the provided {@code profile} for the provided {@code metaclass}.
     *
     * @param profile
     *            the {@link Profile} to check
     * @param metaclass
     *            the metaclass to find in the provided {@link Profile}
     * @return {@link ElementImport} contained by the provided {@code profile} for the provided {@code metaclass}, , or
     *         an empty {@link Optional} if not found.
     */
    private Optional<ElementImport> getImportElementForMetaclass(Profile profile, Class metaclass) {
        return profile.getElementImports().stream() //
                .filter(elementImport -> Objects.equals(elementImport.getImportedElement(), metaclass)).findFirst();
    }

    private Optional<Node> getDiagramElement(IEditingContext editingContext, Diagram diagram, String diagramElementId) {
        return this.getDiagramNavigationService() //
                .getMatchingNodes(diagram, editingContext, NodeMatcher.buildSemanticAndNodeMatcher(BorderNodeStatus.BASIC_NODE, null, n -> n.getId().equals(diagramElementId)))//
                .stream() //
                .findFirst();
    }
}
