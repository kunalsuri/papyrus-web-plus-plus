/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 210, 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.IViewQuerier;
import org.eclipse.papyrus.uml.domain.services.create.CreationStatus;
import org.eclipse.papyrus.uml.domain.services.create.ElementBasedEdgeCreator;
import org.eclipse.papyrus.uml.domain.services.create.ElementConfigurer;
import org.eclipse.papyrus.uml.domain.services.create.ElementCreationChecker;
import org.eclipse.papyrus.uml.domain.services.create.ElementCreator;
import org.eclipse.papyrus.uml.domain.services.create.ElementDomainBasedEdgeCreationChecker;
import org.eclipse.papyrus.uml.domain.services.create.ICreator;
import org.eclipse.papyrus.uml.domain.services.create.ICreatorChecker;
import org.eclipse.papyrus.uml.domain.services.create.IDomainBasedEdgeCreationChecker;
import org.eclipse.papyrus.uml.domain.services.create.IDomainBasedEdgeCreator;
import org.eclipse.papyrus.uml.domain.services.destroy.DestroyerStatus;
import org.eclipse.papyrus.uml.domain.services.destroy.ElementDestroyer;
import org.eclipse.papyrus.uml.domain.services.destroy.IDestroyer;
import org.eclipse.papyrus.uml.domain.services.directedit.ElementDirectEditInputValueProvider;
import org.eclipse.papyrus.uml.domain.services.directedit.ElementDirectEditValueConsumer;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeContainerProvider;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeInitializer;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeSourceProvider;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeTargetsProvider;
import org.eclipse.papyrus.uml.domain.services.edges.IDomainBasedEdgeSourceProvider;
import org.eclipse.papyrus.uml.domain.services.edges.IDomainBasedEdgeTargetsProvider;
import org.eclipse.papyrus.uml.domain.services.labels.ElementDomainBasedEdgeSourceLabelProvider;
import org.eclipse.papyrus.uml.domain.services.labels.ElementDomainBasedEdgeTargetLabelProvider;
import org.eclipse.papyrus.uml.domain.services.labels.ElementLabelProvider;
import org.eclipse.papyrus.uml.domain.services.labels.ElementLabelProvider.Builder;
import org.eclipse.papyrus.uml.domain.services.labels.IViewLabelProvider;
import org.eclipse.papyrus.uml.domain.services.labels.KeywordLabelProvider;
import org.eclipse.papyrus.uml.domain.services.labels.StereotypeLabelPrefixProvider;
import org.eclipse.papyrus.uml.domain.services.labels.domains.DefaultNamedElementNameProvider;
import org.eclipse.papyrus.uml.domain.services.modify.ElementFeatureModifier;
import org.eclipse.papyrus.uml.domain.services.modify.IFeatureModifier;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger.ILogLevel;
import org.eclipse.papyrus.uml.domain.services.reconnect.ElementDomainBasedEdgeReconnectSourceBehaviorProvider;
import org.eclipse.papyrus.uml.domain.services.reconnect.ElementDomainBasedEdgeReconnectTargetBehaviorProvider;
import org.eclipse.papyrus.uml.domain.services.reconnect.ElementDomainBasedEdgeReconnectionSourceChecker;
import org.eclipse.papyrus.uml.domain.services.reconnect.ElementDomainBasedEdgeReconnectionTargetChecker;
import org.eclipse.papyrus.uml.domain.services.status.CheckStatus;
import org.eclipse.papyrus.uml.domain.services.status.State;
import org.eclipse.papyrus.uml.domain.services.status.Status;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.WebDiagramElementCreator;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.GenericWebExternalDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.GenericWebInternalDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.WebRepresentationQuerier;
import org.eclipse.papyrus.web.sirius.contributions.AqlServiceClass;
import org.eclipse.papyrus.web.sirius.contributions.DiagramElementHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DeletionPolicy;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.diagrams.elements.NodeElementProps;
import org.eclipse.sirius.components.diagrams.renderer.DiagramRenderingCache;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation for service on a diagram.
 *
 * @author Arthur Daussy
 */
@AqlServiceClass
public abstract class AbstractDiagramService {

    // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
    public static final EObject FAILURE_OBJECT = EcoreFactory.eINSTANCE.createEObject();

    /**
     * Profile extension name.
     */
    public static final String PROFILE_EXT = ".profile.uml";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiagramService.class);

    private static final String ITEM_SEP = ",";

    private final IObjectService objectService;

    private final IDiagramNavigationService diagramNavigationService;

    private final IDiagramOperationsService diagramOperationsService;

    private final IEditableChecker editableChecker;

    private IViewDiagramDescriptionService viewDiagramService;

    /**
     * Logger used to report errors and warnings to the user.
     */
    private ILogger logger;

    public AbstractDiagramService(IObjectService objectService, IDiagramNavigationService diagramNavigationService, IDiagramOperationsService diagramOperationsService,
            IEditableChecker editableChecker, IViewDiagramDescriptionService viewDiagramService, ILogger logger) {
        this.editableChecker = editableChecker;
        this.viewDiagramService = Objects.requireNonNull(viewDiagramService);
        this.objectService = Objects.requireNonNull(objectService);
        this.diagramNavigationService = Objects.requireNonNull(diagramNavigationService);
        this.diagramOperationsService = Objects.requireNonNull(diagramOperationsService);
        this.logger = logger;
    }

    protected IObjectService getObjectService() {
        return this.objectService;
    }

    protected IDiagramNavigationService getDiagramNavigationService() {
        return this.diagramNavigationService;
    }

    protected IDiagramOperationsService getDiagramOperationsService() {
        return this.diagramOperationsService;
    }

    protected IEditableChecker getEditableChecker() {
        return this.editableChecker;
    }

    /**
     * Prevents displaying between element are displayed as children. The containment link should only be display in
     * case of sibling elements.
     *
     * @param semanticSource
     *            the semantic source of the edge
     * @param semanticTarget
     *            the semantic target of the edge
     * @param ancestorCandidate
     *            the visual source of the edge candidate
     * @param descendantCandidate
     *            the visual target of the edge candidate
     * @param cache
     *            the {@link DiagramRenderingCache}
     * @return <code>true</code> if the edge should be displayed, <code>false</code> otherwise
     */
    public boolean isNotVisualDescendant(org.eclipse.sirius.components.representations.Element ancestorCandidate, org.eclipse.sirius.components.representations.Element descendantCandidate,
            DiagramRenderingCache cache) {
        // Prevents rendering if the target node is already contained in source node

        Optional<String> visualTargetId = new DiagramElementHelper(descendantCandidate).getId();
        Optional<String> visualSourceId = new DiagramElementHelper(ancestorCandidate).getId();

        Set<String> ancestorIds = visualTargetId.map(id -> cache.getAncestors(id).stream()//
                .filter(e -> e.getProps() instanceof NodeElementProps)//
                .map(e -> ((NodeElementProps) e.getProps()).getId())//
                .collect(toSet())).orElse(Collections.emptySet());
        return visualSourceId.isPresent() && !ancestorIds.contains(visualSourceId.get());
    }

    /**
     * Gets the semantic source of domain base edge from its semantic element.
     *
     * @param semanticElementEdge
     *            the semantic element of the domain based edge
     * @return a source element or <code>null</code>
     */
    public EObject getSource(EObject semanticElementEdge) {
        return this.buildEdgeSourceProvider().getSource(semanticElementEdge);

    }

    /**
     * Provides a input text when a direct edit tool is called.
     *
     * @param semanticElement
     *            the semantic element
     * @return a text (never <code>null</code>)
     */
    public String getDirectEditInputValue(EObject semanticElement) {
        return new ElementDirectEditInputValueProvider().getDirectEditInputValue(semanticElement);
    }

    /**
     * Consumes the value after a direct edit tool execution.
     *
     * @param semanticElement
     *            the semantic element on which the direct tool has been executed
     * @param newValue
     *            the edited value
     */
    public void consumeDirectEditValue(EObject semanticElement, String newValue) {
        String oldValue = this.getDirectEditInputValue(semanticElement);
        if (!oldValue.equals(newValue)) {
            new ElementDirectEditValueConsumer().consumeNewLabel(semanticElement, newValue);
        }
    }

    protected IDomainBasedEdgeSourceProvider buildEdgeSourceProvider() {
        return new ElementDomainBasedEdgeSourceProvider();
    }

    /**
     * Gets the semantic targets of domain base edge from its semantic element.
     *
     * @param semanticElementEdge
     *            the semantic element of the domain based edge
     * @return a list of target element
     */
    public List<? extends EObject> getTargets(EObject semanticElementEdge) {
        return this.buildEdgeTargetsProvider().getTargets(semanticElementEdge);
    }

    private IDomainBasedEdgeTargetsProvider buildEdgeTargetsProvider() {
        return new ElementDomainBasedEdgeTargetsProvider();
    }

    /**
     * Log a given message in developer logger and user interface.
     *
     * @param message
     *            the message to display
     */
    private void logWarnMessage(String message) {
        LOGGER.warn(message);
        this.logger.log(message, ILogLevel.WARNING);
    }

    /**
     * Destroys the given semantic element represented by a node.
     *
     * @param semanticElement
     *            a semantic element
     * @param diagramContext
     *            the {@link IDiagramContext}
     * @param targetView
     *            the representation of the semantic object to delete
     * @param deletionPolicy
     *            check if it is a visual or semantic deletion
     *
     * @return a useless EObject see https://github.com/eclipse-sirius/sirius-components/issues/1343
     */
    public EObject destroy(EObject semanticElement, IDiagramContext diagramContext, Node targetView, DeletionPolicy deletionPolicy) {
        switch (deletionPolicy) {
            case GRAPHICAL:
                if (targetView != null) {
                    this.diagramOperationsService.deleteView(diagramContext, targetView);
                }
                break;
            case SEMANTIC:
                if (semanticElement != null) {
                    ECrossReferenceAdapter adapter = this.getECrossReferenceAdapter(semanticElement);
                    DestroyerStatus destroyerStatus = this.buildDestroyer(adapter).destroy(semanticElement);

                    if (State.FAILED.equals(destroyerStatus.getState())) {
                        String elements = destroyerStatus.getElements().stream()//
                                .map(Object::toString)//
                                .collect(Collectors.joining(ITEM_SEP));
                        String errorMessage = destroyerStatus.getMessage() + ": " + elements;
                        this.logWarnMessage(errorMessage);
                    }
                }
                break;
            default:
                break;
        }
        // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
        EObject result = FAILURE_OBJECT;
        return result;
    }

    protected IDestroyer buildDestroyer(ECrossReferenceAdapter adapter) {
        return ElementDestroyer.buildDefault(adapter, this.editableChecker);
    }

    /**
     * Destroys the given semantic element represented by a edge.
     *
     * @param semanticElement
     *            a semantic element
     * @param diagramContext
     *            the {@link IDiagramContext}
     * @param targetView
     *            the representation of the semantic object to delete
     * @param deletionPolicy
     *            check if it is a visual or semantic deletion
     *
     * @return a useless EObject see https://github.com/eclipse-sirius/sirius-components/issues/1343
     */
    public EObject destroy(EObject semanticElement, IDiagramContext diagramContext, Edge targetView, DeletionPolicy deletionPolicy) {
        switch (deletionPolicy) {
            case GRAPHICAL:
                // Do nothing for now since all edge are synchronized
                // Needs to be implements once the unsynchronized edges are implemented
                break;
            case SEMANTIC:
                if (semanticElement != null) {
                    ECrossReferenceAdapter adapter = this.getECrossReferenceAdapter(semanticElement);
                    DestroyerStatus destroyerStatus = this.buildDestroyer(adapter).destroy(semanticElement);

                    if (State.FAILED.equals(destroyerStatus.getState())) {
                        String elements = destroyerStatus.getElements().stream()//
                                .map(Object::toString)//
                                .collect(Collectors.joining(ITEM_SEP));
                        String errorMessage = destroyerStatus.getMessage() + ": " + elements;
                        this.logWarnMessage(errorMessage);
                    }
                }
                break;
            default:
                break;
        }
        // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
        EObject result = FAILURE_OBJECT;
        return result;
    }

    /**
     * Drop an element.
     *
     * @param semanticDroppedElement
     *            the dropped element
     * @param targetView
     *            the view on which the element is dropped (<code>null</code> if dropped on the diagram)
     * @param editionContext
     *            the {@link IEditingContext}
     * @param diagramContext
     *            the {@link IDiagramContext}
     * @param capturedNodeDescriptions
     *            a map of all converted node descriptions of the current diagram description (
     *            {@link org.eclipse.sirius.components.view.NodeDescription} -> {@link NodeDescription})
     * @return self (required for the service to create a view - convention on Sirius component)
     */
    public EObject semanticDrop(EObject droppedElement, Node targetView, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        this.buildSemanticDropBehaviorProvider(droppedElement, editionContext, diagramContext, capturedNodeDescriptions).handleSemanticDrop(droppedElement, targetView);
        return droppedElement;
    }

    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject droppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, org.eclipse.sirius.components.diagrams.description.NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getObjectService(), this.viewDiagramService, this.getDiagramOperationsService(), diagramContext, capturedNodeDescriptions);
        return new GenericWebExternalDropBehaviorProvider(createViewHelper, new DiagramNavigator(this.diagramNavigationService, diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
    }

    public EObject graphicalDrop(EObject droppedElement, EObject targetElement, Node droppedView, Node targetView, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        this.buildGraphicalDropBehaviorProvider(droppedElement, editionContext, diagramContext, capturedNodeDescriptions).handleGraphicalDrop(droppedElement, targetElement, droppedView, targetView);
        return droppedElement;
    }

    protected IWebInternalSourceToRepresentationDropBehaviorProvider buildGraphicalDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, org.eclipse.sirius.components.diagrams.description.NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getObjectService(), this.viewDiagramService, this.getDiagramOperationsService(), diagramContext, capturedNodeDescriptions);
        return new GenericWebInternalDropBehaviorProvider(createViewHelper, new DiagramNavigator(this.diagramNavigationService, diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
    }

    /**
     * Add the given value in the feature of the given featureOwner.
     *
     * @param featureOwner
     *            the modified {@link EObject} (owning the feature to modified)
     * @param featureName
     *            the name of the feature to modify
     * @param value
     *            the value to add
     * @return
     */
    public EObject addValueTo(EObject featureOwner, String featureName, Object value) {
        Status status = this.buildFeatureModifier(featureOwner).addValue(featureOwner, featureName, value);
        if (status.getState() == State.FAILED) {
            this.logWarnMessage(status.getMessage());
        }
        return featureOwner;
    }

    /**
     * Removes the value from a given feature.
     *
     * @param featureOwner
     *            the feature owner
     * @param featureName
     *            the name of the feature
     * @param value
     *            the value to remove
     * @return the feature owner
     */
    public EObject removeValueFrom(EObject featureOwner, String featureName, Object value) {
        Status status = this.buildFeatureModifier(featureOwner).removeValue(featureOwner, featureName, value);
        if (status.getState() == State.FAILED) {
            this.logWarnMessage(status.getMessage());
        }
        return featureOwner;
    }

    protected IFeatureModifier buildFeatureModifier(EObject featureOwner) {
        return new ElementFeatureModifier(this.getECrossReferenceAdapter(featureOwner), this.editableChecker);
    }

    /**
     * Render the label for a given semantic {@link EObject}.
     *
     * @param semanticObject
     *            a semantic element
     * @return a label
     */
    public String renderLabel(EObject semanticObject) {
        return this.buildLabelRenderer().getLabel(semanticObject);
    }

    /**
     * Render the label of given semantic {@link EObject} on one line.
     *
     * @param semanticObject
     *            the semantic element
     * @param displayKeyword
     *            holds <code>true</code> to display the keywords
     * @param displayStereotypes
     *            holds <code>true</code> to display the stereotypes
     * @return a label
     */
    public String renderSimpleOneLineLabel(EObject semanticObject, boolean displayKeyword, boolean displayStereotypes) {
        Builder builder = ElementLabelProvider.builder()//
                .withNameProvider(new DefaultNamedElementNameProvider());

        if (displayStereotypes) {
            builder.withPrefixLabelProvider(new StereotypeLabelPrefixProvider())//
                    .withPrefixSeparator(" ");
        }
        if (displayKeyword) {
            builder.withPrefixLabelProvider(new KeywordLabelProvider())//
                    .withKeywordSeparator(" ");
        }
        return builder.build().getLabel(semanticObject);
    }

    protected IViewLabelProvider buildLabelRenderer() {
        return ElementLabelProvider.buildDefault();
    }

    /**
     * Service used to create a domain base edge.
     *
     * @param source
     *            the semantic source
     * @param target
     *            the semantic target
     * @param type
     *            the new element type
     * @param containementReferenceName
     *            the containment reference name
     * @param sourceNode
     *            the source {@link Node} of the new edge
     * @param targetNode
     *            the target {@link Node} of the new edge
     * @param editingContext
     *            the current {@link IEditingContext}
     * @param diagramContext
     *            the current {@link IDiagramContext}
     * @return a new element or <code>null</code>
     */
    // CHECKSTYLE:OFF All the parameters are required. We could remove source and target and recompute them using the
    // IObjectService. But since it is done in Sirius component it would be a shame
    public EObject createDomainBasedEdge(EObject source, EObject target, String type, String containementReferenceName, Node sourceNode, Node targetNode, IEditingContext editingContext,
            IDiagramContext diagramContext) {

        String errorMessage = null;
        IViewQuerier represenationQuery = this.createRepresentationQuerier(editingContext, diagramContext.getDiagram());

        // Workaround for missing precondition on edges
        CheckStatus canCreateStatus = this.buildDomainBasedEdgeCreationChecker().canCreate(source, target, type, containementReferenceName, represenationQuery, sourceNode, targetNode);
        final EObject result;
        if (!canCreateStatus.isValid()) {
            errorMessage = "Creation failed : " + canCreateStatus.getMessage();
            this.logWarnMessage(errorMessage);
            result = null;
        } else {
            CreationStatus status = this.buildDomainBasedEdgeCreator(source).createDomainBasedEdge(source, target, type, containementReferenceName, represenationQuery, sourceNode, targetNode);

            result = status.getElement();

            if (status.getState() == State.FAILED) {
                errorMessage = "Creation failed : " + status.getMessage();
                this.logWarnMessage(errorMessage);
            }

        }
        if (result == null) {
            // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
            return FAILURE_OBJECT;
        }
        return result;
    }
    // CHECKSTYLE:ON

    private WebRepresentationQuerier createRepresentationQuerier(IEditingContext editingContext, Diagram diagram) {
        return new WebRepresentationQuerier(diagram, this.objectService, this.diagramNavigationService, editingContext);
    }

    protected IDomainBasedEdgeCreationChecker buildDomainBasedEdgeCreationChecker() {
        return new ElementDomainBasedEdgeCreationChecker();
    }

    public IViewDiagramDescriptionService getViewDiagramService() {
        return this.viewDiagramService;
    }

    protected ICreatorChecker buildElementCreationChecker() {
        return new ElementCreationChecker();
    }

    protected IDomainBasedEdgeCreator buildDomainBasedEdgeCreator(EObject source) {
        ElementBasedEdgeCreator baseEdgeCreator = new ElementBasedEdgeCreator(//
                new ElementDomainBasedEdgeContainerProvider(this.getEditableChecker()), //
                new ElementDomainBasedEdgeInitializer(), //
                new ElementConfigurer(), //
                new ElementFeatureModifier(this.getECrossReferenceAdapter(source), this.getEditableChecker()));
        return baseEdgeCreator;
    }

    protected ECrossReferenceAdapter getECrossReferenceAdapter(EObject source) {
        return source.eResource().getResourceSet().eAdapters().stream()//
                .filter(a -> a instanceof ECrossReferenceAdapter)//
                .map(a -> (ECrossReferenceAdapter) a)//
                .findFirst().orElse(null);
    }

    /**
     * Creates a new semantic element, initialize and create a view.
     *
     * @param parent
     *            the semantic parent
     * @param type
     *            the type of element to create
     * @param referenceName
     *            the name of the containment reference
     * @param targetView
     *            the view on which the creation has been requested (<code>null</code> if request on the diagram root)
     * @param diagramContext
     *            the {@link IDiagramContext}
     * @param capturedNodeDescriptions
     *            a map of all converted node descriptions of the current diagram description (
     *            {@link org.eclipse.sirius.components.view.NodeDescription} -> {@link NodeDescription})
     * @return a new instance or <code>null</code> if the creation failed
     */
    public EObject create(EObject parent, String type, String referenceName, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        final EObject result;
        String errorMessage = null;
        if (parent == null) {
            errorMessage = "Unable to create an element on nothing";
            this.logWarnMessage(errorMessage);
            result = null;
        } else {
            // Workaround for missing precondition on edges
            CheckStatus canCreateStatus = this.buildElementCreationChecker().canCreate(parent, type, referenceName);
            if (!canCreateStatus.isValid()) {
                errorMessage = "Can not create : " + canCreateStatus.getMessage();
                this.logWarnMessage(errorMessage);
                result = null;
            } else {

                WebDiagramElementCreator elementCreator = new WebDiagramElementCreator(this.buildElementCreator(parent), this.objectService, this.viewDiagramService, this.diagramOperationsService);
                CreationStatus status = elementCreator.handleCreation(parent, type, referenceName, targetView, diagramContext, capturedNodeDescriptions);
                result = status.getElement();

                if (status.getState() == State.FAILED) {
                    errorMessage = "Creation failed : " + status.getMessage();
                    this.logWarnMessage(errorMessage);
                }
            }
        }
        if (result == null) {
            // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
            return FAILURE_OBJECT;
        }
        return result;

    }

    /**
     * Creates a new semantic element, initialize and create a view in the parent (supposed to be the holder).
     *
     * @param parent
     *            the semantic parent
     * @param type
     *            the type of element to create
     * @param referenceName
     *            the name of the containment reference
     * @param targetView
     *            the view on which the creation has been requested (<code>null</code> if request on the diagram root)
     * @param diagramContext
     *            the {@link IDiagramContext}
     * @param capturedNodeDescriptions
     *            a map of all converted node descriptions of the current diagram description (
     *            {@link org.eclipse.sirius.components.view.NodeDescription} -> {@link NodeDescription})
     * @return a new instance or <code>null</code> if the creation failed
     */
    public EObject createInHolder(EObject parent, String type, String referenceName, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.create(parent, type, referenceName, this.getParentNode(targetView, diagramContext.getDiagram()), diagramContext, capturedNodeDescriptions);
    }

    /**
     * Creates a new semantic element inside compartment, initialize and create a view.
     *
     * @param parent
     *            the semantic parent
     * @param type
     *            the type of element to create
     * @param referenceName
     *            the name of the containment reference
     * @param compartmentName
     *            the name of the compartment which will contain the new element
     * @param targetView
     *            the view on which the creation has been requested (<code>null</code> if request on the diagram root)
     * @param diagramContext
     *            the {@link IDiagramContext}
     * @param capturedNodeDescriptions
     *            a map of all converted node descriptions of the current diagram description (
     *            {@link org.eclipse.sirius.components.view.NodeDescription} -> {@link NodeDescription})
     * @return a new instance or <code>null</code> if the creation failed
     */
    public EObject createInCompartment(EObject parent, String type, String compartmentName, String referenceName, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {

        DiagramNavigator navigator = new DiagramNavigator(this.diagramNavigationService, diagramContext.getDiagram(), capturedNodeDescriptions);
        Node compartmentNode = null;
        for (Node node : targetView.getChildNodes()) {
            Optional<org.eclipse.sirius.components.view.diagram.NodeDescription> nodeDescription = navigator.getDescription(node);
            if (nodeDescription.isPresent() && nodeDescription.get().getName().contains(compartmentName)) {
                compartmentNode = node;
                break;
            }
        }
        return this.create(parent, type, referenceName, compartmentNode, diagramContext, capturedNodeDescriptions);
    }

    @Deprecated
    public EObject createSibling(EObject sibling, String type, String referenceName, Node siblingView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        EObject parent = sibling.eContainer();
        return new DiagramNavigator(this.diagramNavigationService, diagramContext.getDiagram(), capturedNodeDescriptions).getParentNode(siblingView).map(parentNode -> {
            return this.create(parent, type, referenceName, parentNode, diagramContext, capturedNodeDescriptions);
        }).orElseGet(() -> {
            String errorMessage = MessageFormat.format("Unable to get the parent view of {0}", sibling);
            this.logWarnMessage(errorMessage);
            return sibling;
        });
    }

    /**
     * Reconnection service for the source of a DomainBasedEdge.
     *
     * @param domainBaseEdge
     *            the semantic element of the edge
     * @param oldSource
     *            the old source of the edge
     * @param newSource
     *            the new source of the edge
     * @return the semantic edge
     */
    public EObject reconnectSourceOnDomainBasedEdge(EObject domainBaseEdge, EObject oldSource, EObject newSource, Node reconnectionTargetView, Node reconnectionSourceView,
            IEditingContext editingContext, Diagram diagram) {
        IViewQuerier represenationQuery = this.createRepresentationQuerier(editingContext, diagram);
        CheckStatus checkStatus = new ElementDomainBasedEdgeReconnectionSourceChecker(this.getEditableChecker(), represenationQuery).canReconnect(domainBaseEdge, oldSource, newSource,
                reconnectionTargetView, reconnectionSourceView);
        if (checkStatus.isValid()) {
            CheckStatus status = new ElementDomainBasedEdgeReconnectSourceBehaviorProvider(this.getEditableChecker(), represenationQuery).reconnectSource(domainBaseEdge, oldSource, newSource,
                    reconnectionTargetView);
            if (!status.isValid()) {
                this.logWarnMessage(status.getMessage());
            }
        } else {
            this.logWarnMessage(checkStatus.getMessage());
        }
        return domainBaseEdge;
    }

    /**
     * Computes the source label (or begin label) of a domain base edge.
     *
     * @param domainBaseEdge
     *            the semantic element of the domain base edge
     * @param source
     *            the semantic element of the source
     * @return a label
     */
    public String getDomainBasedEdgeSourceLabel(EObject domainBaseEdge, EObject source) {
        return ElementDomainBasedEdgeSourceLabelProvider.buildDefault().getLabel(domainBaseEdge, source);
    }

    /**
     * Computes the target label (or end label) of a domain base edge.
     *
     * @param domainBaseEdge
     *            the semantic element of the domain base edge
     * @param target
     *            the semantic element of the target
     * @return a label
     */
    public String getDomainBasedEdgeTargetLabel(EObject domainBaseEdge, EObject target) {
        return ElementDomainBasedEdgeTargetLabelProvider.buildDefault().getLabel(domainBaseEdge, target);
    }

    /**
     * Reconnection service for the target of a DomainBasedEdge.
     *
     * @param domainBaseEdge
     *            the semantic element of the edge
     * @param oldTarget
     *            the old target of the edge
     * @param newTarget
     *            the new target of the edge
     * @return the semantic edge
     */
    public EObject reconnectTargetOnDomainBasedEdge(EObject domainBaseEdge, EObject oldTarget, EObject newTarget, Node reconnectionTargetView, Node reconnectionSourceView,
            IEditingContext editingContext, Diagram diagram) {
        IViewQuerier represenationQuery = this.createRepresentationQuerier(editingContext, diagram);
        CheckStatus checkStatus = new ElementDomainBasedEdgeReconnectionTargetChecker(this.getEditableChecker(), represenationQuery).canReconnect(domainBaseEdge, oldTarget, newTarget,
                reconnectionTargetView, reconnectionSourceView);
        if (checkStatus.isValid()) {
            CheckStatus status = new ElementDomainBasedEdgeReconnectTargetBehaviorProvider(represenationQuery).reconnectTarget(domainBaseEdge, oldTarget, newTarget, reconnectionTargetView);
            if (!status.isValid()) {
                this.logWarnMessage(status.getMessage());
            }
        } else {
            this.logWarnMessage(checkStatus.getMessage());
        }
        return domainBaseEdge;
    }

    protected ICreator buildElementCreator(EObject parent) {
        return new ElementCreator(new ElementConfigurer(), new ElementFeatureModifier(this.getECrossReferenceAdapter(parent), this.getEditableChecker()));
    }

    /**
     * Target reconnection service for the annotated element feature of a {@link Comment}.
     *
     * @param source
     *            the current source of the edge
     * @param oldTarget
     *            the old target
     * @param newTarget
     *            the new target
     * @return the current source
     */
    public EObject reconnectCommentAnnotatedElementEdgeTarget(Comment source, EObject oldTarget, EObject newTarget) {

        if (newTarget instanceof Element) {
            source.getAnnotatedElements().remove(oldTarget);
            source.getAnnotatedElements().add((Element) newTarget);
        } else {
            String errorMessage = "Can't reconnect to the new target. It is not an Element";
            this.logWarnMessage(errorMessage);
        }

        return source;

    }

    /**
     * Target reconnection service for the constrained element feature of a {@link Constraint}.
     *
     * @param source
     *            the current source of the edge
     * @param oldTarget
     *            the old target
     * @param newTarget
     *            the new target
     * @return the current source
     */
    public EObject reconnectConstraintConstrainedElementEdgeTarget(Constraint source, EObject oldTarget, EObject newTarget) {
        if (newTarget instanceof Element element) {
            source.getConstrainedElements().remove(oldTarget);
            source.getConstrainedElements().add(element);
        } else {
            String errorMessage = "Can't reconnect to the new target. It is not an Element";
            this.logWarnMessage(errorMessage);
        }
        return source;
    }

    /**
     * Move the target element inside the feature owner.
     *
     * <p>
     * This method check that this operation will no create a containment loop
     * </p>
     *
     * @param objectToMove
     *            the object to move
     * @param newOwner
     *            the future owner
     * @return the future owner if success, a mocked EObject otherwise
     *         (https://github.com/eclipse-sirius/sirius-components/issues/1343)
     */
    public EObject moveIn(EObject objectToMove, EObject newOwner) {
        List<EReference> candidateReferences = this.getUMLContainementReference(newOwner, objectToMove);
        String errorMessage = null;
        final EReference ref;
        if (candidateReferences.isEmpty()) {
            errorMessage = MessageFormat.format("Impossible for a {0} to contain a {1}", newOwner.eClass().getName(), objectToMove);
            this.logWarnMessage(errorMessage);
            ref = null;
        } else {
            ref = candidateReferences.get(0);
            if (candidateReferences.size() > 1) {
                errorMessage = MessageFormat.format("More than one containment reference to contain {0} : {1}", objectToMove.eClass().getName(),
                        candidateReferences.stream().map(f -> f.getName()).collect(joining(ITEM_SEP)));
                this.logWarnMessage(errorMessage);
            }
        }

        final EObject result;
        if (ref != null) {
            result = this.moveIn(objectToMove, newOwner, ref);
        } else {
            result = AbstractDiagramService.FAILURE_OBJECT;
        }

        return result;

    }

    /**
     * Moves the target element inside the feature owner.
     *
     * <p>
     * This method check that this operation will no create a containment loop
     * </p>
     *
     * @param objectToMove
     *            the object to move
     * @param newOwner
     *            the future owner
     * @param eRefName
     *            name of the containment reference
     * @return the future owner if success, a mocked EObject otherwise
     *         (https://github.com/eclipse-sirius/sirius-components/issues/1343)
     */
    public EObject moveIn(EObject objectToMove, EObject newOwner, String eRefName) {
        EStructuralFeature refCandidate = newOwner.eClass().getEStructuralFeature(eRefName);

        final EReference ref;
        if (!(refCandidate instanceof EReference) || !((EReference) refCandidate).isContainment()) {
            String errorMessage = MessageFormat.format("Impossible for a {0} to contain a {1}", newOwner.eClass().getName(), objectToMove);
            this.logWarnMessage(errorMessage);
            ref = null;
        } else {
            ref = (EReference) refCandidate;
        }

        final EObject result;
        if (ref != null) {
            result = this.moveIn(objectToMove, newOwner, ref);
        } else {
            result = AbstractDiagramService.FAILURE_OBJECT;
        }

        return result;

    }

    private EObject moveIn(EObject objectToMove, EObject newOwner, final EReference ref) {
        final EObject result;
        if (EMFUtils.getAncestors(EObject.class, newOwner).contains(objectToMove)) {
            String errorMessage = MessageFormat.format("Impossible to move this {0} in this {1}. It would create a containment loop.", objectToMove.eClass().getName(), newOwner.eClass().getName());
            this.logWarnMessage(errorMessage);
            // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
            result = AbstractDiagramService.FAILURE_OBJECT;
        } else {
            result = this.addValueTo(newOwner, ref.getName(), objectToMove);
        }
        return result;
    }

    private List<EReference> getUMLContainementReference(EObject owner, EObject child) {
        return owner.eClass().getEAllContainments().stream()//
                .filter(f -> f.getEType().isInstance(child))//
                .filter(f -> EMFUtils.getAncestor(EPackage.class, f) == UMLPackage.eINSTANCE)// Only use UML feature
                                                                                             // (not ecore)
                .collect(toList());
    }

    /**
     * Source reconnection service for the annotated element feature of a {@link Comment}.
     *
     * @param target
     *            the current target of the edge
     * @param oldSource
     *            the old source
     * @param newSource
     *            the new source
     * @return the current target
     */
    public EObject reconnectCommentAnnotatedElementEdgeSource(EObject target, Comment oldSource, Comment newSource) {

        if (target instanceof Element) {
            oldSource.getAnnotatedElements().remove(target);
            newSource.getAnnotatedElements().add((Element) target);
        } else {
            String errorMessage = "The target element should be an element";
            this.logWarnMessage(errorMessage);
        }

        return target;
    }

    /**
     * Source reconnection service for the constrained element feature of a {@link Constraint}.
     *
     * @param target
     *            the current target of the edge
     * @param oldSource
     *            the old source
     * @param newSource
     *            the new source
     * @return the current target
     */
    public EObject reconnectConstraintConstrainedElementEdgeSource(EObject target, Constraint oldSource, Constraint newSource) {
        if (target instanceof Element element) {
            oldSource.getConstrainedElements().remove(element);
            newSource.getConstrainedElements().add(element);
        } else {
            String errorMessage = "The target element should be an Element";
            this.logWarnMessage(errorMessage);
        }
        return target;
    }

    /**
     * Check if the resource of a given {@code context} contains the ".profile.uml" extension in its name.
     *
     * @param context
     *            context used to create diagram on
     *
     * @return {@code true} if the resource contains the ".profile.uml" extension, {@code false} otherwise
     */

    protected boolean isContainedInProfileResource(EObject context) {
        if (context != null && context.eResource() instanceof JsonResource jsonResource) {
            for (Adapter adapter : jsonResource.eAdapters()) {
                if (adapter instanceof ResourceMetadataAdapter resourceMetadataAdapter) {
                    return resourceMetadataAdapter.getName().endsWith(PROFILE_EXT);
                }
            }
        }
        return false;
    }

    /**
     * Check if the resource of a given {@link Object} is not a Profile model.
     *
     * @param context
     *            context used to create diagram on
     *
     * @return <code>true</code> if the resource is a profile model, <code>false</code> otherwise.
     */
    public boolean isNotProfileModel(EObject context) {
        return !this.isContainedInProfileResource(context);
    }

    /**
     * Check if the nodes is a Symbol or not.
     *
     * @param context
     *            context used to create diagram on
     *
     * @return <code>true</code> if the resource is a profile model, <code>false</code> otherwise.
     */
    public List<Node> getAllSymbol(DiagramContext diagramContext) {
        return diagramContext.getDiagram().getNodes();
    }

    /**
     * Create an {@link EAnnotation} and add it to the context.
     *
     * @param context
     *            context to create the annotation on
     * @return context on which the annotation was added
     */
    public EModelElement addAnnotation(EModelElement context) {
        EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
        context.getEAnnotations().add(annotation);
        return context;
    }

    /**
     * Remove any {@link EAnnotation} from the context.
     *
     * @param context
     *            context to remove the annotations from
     * @return context on which the annotation was removed
     */
    public EModelElement removeAnnotation(EModelElement context) {
        while (context.getEAnnotations().size() > 0) {
            context.getEAnnotations().remove(0);
        }
        return context;
    }

    /**
     * Get the parent node of the current one (or the diagram if no parent where found).
     *
     * @param current
     *            the selected node
     * @param diagram
     *            the current diagram
     * @return the parent node
     */
    public Node getParentNode(Node current, Diagram diagram) {
        Optional<Node> oNode = ViewHelper.getAllNodes(diagram).stream().filter(t -> t.getChildNodes().contains(current)).findFirst();
        if (oNode.isPresent()) {
            return oNode.get();
        } else {
            return null;
        }
    }
}
