/*****************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.utils;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.drop.DnDStatus;
import org.eclipse.papyrus.uml.domain.services.drop.IExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.uml.domain.services.drop.IExternalSourceToRepresentationDropChecker;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeSourceProvider;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeTargetsProvider;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger.ILogLevel;
import org.eclipse.papyrus.uml.domain.services.status.CheckStatus;
import org.eclipse.papyrus.uml.domain.services.status.State;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.InterruptibleActivityRegion;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Relationship;
import org.eclipse.uml2.uml.Transition;

/**
 * Default switch used for semantic drop :
 * <ul>
 * <li>a node on a Diagram or on another node,</li>
 * <li>an edge on the diagram or on a node.</li>
 * </ul>
 *
 * @author Arthur Daussy
 */
public final class SemanticDropSwitch extends AbstractDropSwitch {

    /**
     * Checker in charge of checking if a semantic D&D is possible.
     */
    private IExternalSourceToRepresentationDropChecker dropChecker;

    /**
     * Provider of behavior when dropping semantic elements (from Explorer) to a diagram element.
     */
    private IExternalSourceToRepresentationDropBehaviorProvider dropProvider;

    /**
     * Logger used to report errors and warnings to the user.
     */
    private ILogger logger;

    /**
     * Constructor.
     *
     * @param optionalSelectedNode
     *            the selected node where element should be dropped, empty optional if the element should be dropped on
     *            diagram
     * @param viewHelper
     *            the helper used to create element on a diagram
     * @param diagramNavigator
     *            the helper used to navigate inside a diagram and/or to its description
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    public SemanticDropSwitch(Optional<Node> optionalSelectedNode, IViewHelper viewHelper, DiagramNavigator diagramNavigator, ILogger logger) {
        if (optionalSelectedNode.isPresent()) {
            // case DnD on node
            this.targetNode = Objects.requireNonNull(optionalSelectedNode.get());
        } else {
            // case DnD on Diagram
            this.targetNode = null;
        }
        this.viewHelper = viewHelper;
        this.diagramNavigator = diagramNavigator;
        this.logger = logger;
    }

    /**
     * Sets the drop checker used to check if the DragAndDrop is authorized.
     *
     * @param theDropChecker
     *            the dropChecker used to check if the DragAndDrop is authorized
     * @return this SemanticDropSwitch
     */
    public SemanticDropSwitch withDropChecker(IExternalSourceToRepresentationDropChecker theDropChecker) {
        this.dropChecker = theDropChecker;
        return this;
    }

    /**
     * Sets the drop provider used to DragAndDrop an element.
     *
     * @param theDropProvider
     *            the drop provider used to DragAndDrop an element
     * @return this SemanticDropSwitch
     */
    public SemanticDropSwitch withDropProvider(IExternalSourceToRepresentationDropBehaviorProvider theDropProvider) {
        this.dropProvider = theDropProvider;
        return this;
    }

    /**
     * Sets the cross referencer.
     * <p>
     * This parameter is <b>mandatory</b> if the drop checker and drop provider are non null.
     * </p>
     *
     * @param theCrossRef
     *            the cross referencer
     * @return this SemanticDropSwitch
     */
    public SemanticDropSwitch withCrossRef(ECrossReferenceAdapter theCrossRef) {
        this.crossRef = theCrossRef;
        return this;
    }

    /**
     * Sets the editable checker used to check if an element can be edited.
     * <p>
     * This parameter is <b>mandatory</b> if the drop checker and drop provider are non null.
     * </p>
     *
     * @param theEditableChecker
     *            the editable checker
     * @return this SemanticDropSwitch
     */
    public SemanticDropSwitch withEditableChecker(IEditableChecker theEditableChecker) {
        this.editableChecker = theEditableChecker;
        return this;
    }

    /**
     * Sets the eObject resolver used to retrieve the semantic target from the selected node.
     * <p>
     * This parameter is <b>mandatory</b> if the drop checker and drop provider are non null.
     * </p>
     *
     * @param theEObjectResolver
     *            the eEbject resolver
     * @return this SemanticDropSwitch
     */
    public SemanticDropSwitch withEObjectResolver(Function<String, Object> theEObjectResolver) {
        this.eObjectResolver = theEObjectResolver;
        return this;
    }

    @Override
    public Boolean caseRelationship(Relationship relationship) {
        return this.createDnDEdgeView(relationship);
    }

    @Override
    public Boolean caseActivityEdge(ActivityEdge activityEdge) {
        return this.createDnDEdgeView(activityEdge);
    }

    @Override
    public Boolean caseConnector(Connector connector) {
        return this.createDnDEdgeView(connector);
    }

    @Override
    public Boolean caseMessage(Message message) {
        return this.createDnDEdgeView(message);
    }

    @Override
    public Boolean caseTransition(Transition transition) {
        return this.createDnDEdgeView(transition);
    }

    // This case is mandatory because ElementImport is a Relationship so it will
    // work with caseRelationship instead of Default Case
    @Override
    public Boolean caseElementImport(ElementImport elementImport) {
        Boolean isDragAndDropValid = Boolean.FALSE;
        DnDStatus status = null;
        if (this.dropChecker != null && this.dropProvider != null) {
            status = this.semanticDragAndDrop(elementImport);
            if (status != null && status.getState() != State.FAILED) {
                isDragAndDropValid = this.createElementImportDragAndDropView(status);
            }
        } else {
            // default case when no dropChecker neither dropProvider are defined
            // ex. :
            // org.eclipse.papyrus.web.application.representations.aqlservices.utils.GenericWebExternalDropBehaviorProvider
            isDragAndDropValid = this.createDefaultView(elementImport);
        }
        return isDragAndDropValid;
    }

    @Override
    public Boolean defaultCase(EObject object) {
        Boolean isDragAndDropValid = Boolean.FALSE;
        DnDStatus status = null;
        if (this.dropChecker != null && this.dropProvider != null) {
            Objects.requireNonNull(this.crossRef);
            Objects.requireNonNull(this.editableChecker);
            Objects.requireNonNull(this.eObjectResolver);
            status = this.semanticDragAndDrop(object);
            if (status != null && status.getState() != State.FAILED) {
                isDragAndDropValid = this.createDragAndDropView(status);
            }
        } else {
            // default case when no dropChecker neither dropProvider are defined
            // ex. :
            // org.eclipse.papyrus.web.application.representations.aqlservices.utils.GenericWebExternalDropBehaviorProvider
            isDragAndDropValid = this.createDefaultView(object);
        }
        return isDragAndDropValid;
    }

    /**
     * Create views for elements to display after a DragAndDrop.
     *
     * @param status
     *            status of the previous DragAndDrop which contains elements to display
     * @return {@code true} if all views have been created, {@code false} otherwise
     */
    private Boolean createDragAndDropView(DnDStatus status) {
        Boolean isDragAndDropValid = Boolean.TRUE;
        if (status.getState() != State.FAILED) {
            isDragAndDropValid = Boolean.TRUE;
            for (EObject eObjectToDisplay : status.getElementsToDisplay()) {
                if (this.targetNode != null) {
                    // case DnD on Node
                    isDragAndDropValid = isDragAndDropValid && this.createChildView(eObjectToDisplay);
                } else {
                    // case DnD on Diagram
                    isDragAndDropValid = isDragAndDropValid && this.viewHelper.createRootView(eObjectToDisplay);
                }
            }
        } else {
            isDragAndDropValid = Boolean.FALSE;
        }
        return isDragAndDropValid;
    }

    /**
     * Create views for elements to display after an {@link ElementImport} DragAndDrop.
     *
     * @param status
     *            status of the previous {@link ElementImport} DragAndDrop which contains elements to display
     * @return <code>true</code> if all views have been created, <code>false</code> otherwise
     */
    private Boolean createElementImportDragAndDropView(DnDStatus status) {
        Boolean isDragAndDropValid = Boolean.FALSE;
        if (status != null && status.getState() != State.FAILED) {
            isDragAndDropValid = Boolean.TRUE;
            for (EObject eObjectToDisplay : status.getElementsToDisplay()) {
                PackageableElement importedElement = ((ElementImport) eObjectToDisplay).getImportedElement();
                if (importedElement == null) {
                    String errorMesasge = "Only ElementImport with imported element can be drag and dropped";
                    LOGGER.warn(errorMesasge);
                    this.logger.log(status.getMessage(), ILogLevel.WARNING);
                } else if (this.targetNode != null) {
                    // case DnD on Node
                    isDragAndDropValid = isDragAndDropValid && this.createChildView(importedElement, PRDDiagramDescriptionBuilder.PRD_SHARED_METACLASS);
                } else {
                    // case DnD on Diagram
                    isDragAndDropValid = isDragAndDropValid && this.viewHelper.createRootView(importedElement, PRDDiagramDescriptionBuilder.PRD_METACLASS);
                }
            }
        }
        return isDragAndDropValid;
    }

    /**
     * Semantic Drag and drop of a given object on selected node or diagram.
     *
     * @param object
     *            the object to Drag and drop on selected node or diagram
     * @return the status of DragAndDrop
     */
    private DnDStatus semanticDragAndDrop(EObject object) {
        EObject semanticDiagram = this.getSemanticDiagram();
        EObject semanticTarget = null;
        DnDStatus status = null;
        if (this.targetNode != null) {
            // case DnD on Node
            semanticTarget = this.getSemanticNode(this.targetNode);
        } else {
            // case DnD on Diagram
            semanticTarget = semanticDiagram;
        }
        CheckStatus canDragAndDrop = this.dropChecker.canDragAndDrop(object, semanticTarget);
        if (canDragAndDrop.isValid()) {
            status = this.dropProvider.drop(object, semanticTarget, this.crossRef, this.editableChecker);
        } else {
            status = DnDStatus.createFailingStatus(canDragAndDrop.getMessage(), Collections.emptySet());
        }
        if (status.getState() == State.FAILED) {
            LOGGER.warn(status.getMessage());
            this.logger.log(status.getMessage(), ILogLevel.WARNING);
        }
        return status;
    }

    /**
     * Create Edge view, its source view and its target view if needed after DnD of Edge.
     * <p>
     * This method returns {@code true} if the Edge view is created. Since Edges are synchronized this means that the
     * source/target of the Edge are represented on the diagram, and the Edge isn't already represented.
     * </p>
     *
     * @param semanticElementEdge
     *            the semantic element on which the domain based edge is based on
     *
     * @return {@code true} if the Edge view is created, {@code false} otherwise
     */
    private Boolean createDnDEdgeView(final EObject semanticElementEdge) {
        Boolean edgeCreated = Boolean.FALSE;
        if (this.getEdgeFromDiagram(semanticElementEdge).isPresent()) {
            String errorMessage = "Semantic Drag&Drop failed: the Edge " + this.getLabel(semanticElementEdge) + " is already represented on the diagram";
            LOGGER.warn(errorMessage);
            this.logger.log(errorMessage, ILogLevel.WARNING);
        } else {
            // edge are synchronized : no view need to be created
            // only target and source view can be created if they are not already represented on the diagram
            edgeCreated = this.createSourceAndTargetView(semanticElementEdge);
            if (!edgeCreated) {
                String errorMessage = "Semantic Drag&Drop failed : Source and/or Target view of " + this.getLabel(semanticElementEdge) + " edge cannot be created";
                LOGGER.warn(errorMessage);
                this.logger.log(errorMessage, ILogLevel.WARNING);
            }
        }
        return edgeCreated;
    }

    /**
     * Creates the source and target view needed to represent the domain based edge.
     *
     * @param semanticElementEdge
     *            the semantic element on which the domain based edge is based on
     *
     * @return {@code true} if source or target view have been created or if they already exist, {@code false} otherwise
     */
    private Boolean createSourceAndTargetView(EObject semanticElementEdge) {
        Boolean success = Boolean.FALSE;
        Optional<EObject> optionalSemanticSource = Optional.ofNullable(new ElementDomainBasedEdgeSourceProvider().getSource(semanticElementEdge));
        Optional<? extends EObject> optionalSemanticTarget = new ElementDomainBasedEdgeTargetsProvider().getTargets(semanticElementEdge).stream().findFirst();

        if (optionalSemanticSource.isPresent() && optionalSemanticTarget.isPresent()) {
            success = Boolean.TRUE;
            EObject semanticSource = optionalSemanticSource.get();
            EObject semanticTarget = optionalSemanticTarget.get();
            Node sourceNode = this.getNodeFromDiagramAndItsChildren(semanticSource);
            Node targetNode = this.getNodeFromDiagramAndItsChildren(semanticTarget);
            if (sourceNode == null) {
                success = success && this.createEndView(semanticSource);
            }
            if (targetNode == null) {
                if (semanticTarget instanceof Class && ((Class) semanticTarget).isMetaclass()) {
                    ElementImport elementImport = this.getElementImport(semanticElementEdge, semanticTarget);
                    if (elementImport != null) {
                        // case : class is a Metaclass used by element Import
                        // The graphical container of the metaclass is the same as the ElementImport
                        Boolean isTargetCreated = this.createMetaclassEndViewWithContainer(semanticTarget, elementImport.eContainer());
                        success = success && isTargetCreated;
                    } else {
                        // case : class is a simple Metaclass, i.e. a Class with Stereotype
                        Boolean isTargetCreated = this.createEndView(semanticTarget);
                        success = success && isTargetCreated;
                    }
                } else {
                    Boolean isTargetCreated = this.createEndView(semanticTarget);
                    success = success && isTargetCreated;
                }
            }
        }
        return success;
    }

    /**
     * Get {@link ElementImport} of the given semantic element edge resource which refer to a semantic Metaclass target.
     *
     * @param semanticElementEdge
     *            the semantic element on which the domain based edge is based on
     * @param semanticTarget
     *            the semantic target of the Edge to Drag and drop
     *
     * @return {@link ElementImport} of the given semantic element edge resource which refer to a semantic Metaclass
     *         target, null if not found.
     */
    private ElementImport getElementImport(EObject semanticElementEdge, EObject semanticTarget) {
        return EMFUtils.allContainedObjectOfType(semanticElementEdge.eResource(), ElementImport.class)//
                .filter(elementImport -> elementImport.getImportedElement().equals(semanticTarget))//
                .findFirst().orElse(null);
    }

    /**
     * Create semantic element view in its graphical container.
     *
     * @param semanticEnd
     *            the semantic element to represent by a view
     *
     * @return {@code true} if the semantic element view has been created, {@code false} otherwise
     */
    private Boolean createEndView(EObject semanticEnd) {
        EObject semanticEndContainer = semanticEnd.eContainer();
        if (semanticEnd instanceof ActivityNode acitivityNode) {
            EList<ActivityPartition> inPartitions = acitivityNode.getInPartitions();
            if (!inPartitions.isEmpty()) {
                // graphical container is not the semantic container
                semanticEndContainer = inPartitions.get(0);
            }
            EList<InterruptibleActivityRegion> inInterruptibleRegions = acitivityNode.getInInterruptibleRegions();
            if (!inInterruptibleRegions.isEmpty()) {
                // graphical container is not the semantic container
                semanticEndContainer = inInterruptibleRegions.get(0);

            }
        }
        return this.createEndViewWithContainer(semanticEnd, semanticEndContainer);
    }

    /**
     * Create semantic element view in the representation of the given semanticEndContainer.
     *
     * @param semanticEnd
     *            the semantic element to represent by a view
     * @param semanticEndContainer
     *            the semantic container of the view that will contain the semantic end representation
     *
     * @return {@code true} if the semantic element view has been created, {@code false} otherwise
     */
    private Boolean createEndViewWithContainer(EObject semanticEnd, EObject semanticEndContainer) {
        Boolean success = Boolean.FALSE;
        if (semanticEndContainer != null) {
            EObject semanticDiagram = this.getSemanticDiagram();
            if (semanticEndContainer.equals(semanticDiagram) && this.getNodeFromDiagramAndItsChildren(semanticDiagram) == null) {
                success = this.viewHelper.createRootView(semanticEnd);
            } else {
                Node node = this.getNodeFromDiagramAndItsChildren(semanticEndContainer);
                if (node != null) {
                    success = this.createInHolderOrContent(semanticEnd, semanticEndContainer, success, node);
                } else {
                    String errorMessage = "Cannot create view for " + this.getLabel(semanticEnd) + " because its parent node " + this.getLabel(semanticEndContainer) + " is not displayed";
                    LOGGER.warn(errorMessage);
                    this.logger.log(errorMessage, ILogLevel.WARNING);
                }
            }
        }
        return success;
    }

    /**
     * @param semanticEnd
     * @param semanticEndContainer
     * @param success
     * @param node
     * @return
     */
    private Boolean createInHolderOrContent(EObject semanticEnd, EObject semanticEndContainer, Boolean success, Node node) {
        Node nodeContent = this.getNodeFromParentNodeAndItsChildren(node, semanticEndContainer);
        Boolean isCreated = false;
        if (semanticEnd instanceof Port || semanticEnd instanceof Pin) {
            if (!success) {
                isCreated = this.viewHelper.createChildView(semanticEnd, node);
            }
            if (!success && nodeContent != null) {
                isCreated = this.viewHelper.createChildView(semanticEnd, nodeContent);
            }
        } else {
            if (!success && nodeContent != null) {
                isCreated = this.viewHelper.createChildView(semanticEnd, nodeContent);
            }
            if (!isCreated) {
                isCreated = this.viewHelper.createChildView(semanticEnd, node);
            }
        }
        return success || isCreated;
    }

    /**
     * Label displayed to describe a given {@code element}.
     *
     * @param element
     *            the element to describe
     * @return the label displayed to describe an element.
     */
    private String getLabel(EObject element) {
        String label = element.eClass().getName();
        if (element instanceof NamedElement namedElement) {
            String name = namedElement.getName();
            if (name != null && !name.isEmpty()) {
                label = name;
            }
        }
        return label;
    }

    /**
     * Create semantic element view in the representation of the given semanticEndContainer. The semantic element view
     * match with METACLASS mapping (and not CLASS mapping given by default)
     *
     * @param semanticEnd
     *            the semantic element to represent by a view
     * @param semanticEndContainer
     *            the semantic container of the view that will contain the semantic end representation
     *
     * @return {@code true} if the semantic element view has been created, {@code false} otherwise
     */
    private Boolean createMetaclassEndViewWithContainer(EObject semanticEnd, EObject semanticEndContainer) {
        Boolean success = Boolean.FALSE;
        if (semanticEndContainer != null) {
            EObject semanticDiagram = this.getSemanticDiagram();
            if (semanticEndContainer.equals(semanticDiagram)) {
                success = this.viewHelper.createRootView(semanticEnd, PRDDiagramDescriptionBuilder.PRD_METACLASS);
            } else {
                Node node = this.getNodeFromDiagramAndItsChildren(semanticEndContainer);
                success = this.viewHelper.createChildView(semanticEnd, node, PRDDiagramDescriptionBuilder.PRD_SHARED_METACLASS);
            }
        }
        return success;
    }

}
