/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.application.representations.aqlservices.component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.create.ElementBasedEdgeCreator;
import org.eclipse.papyrus.uml.domain.services.create.ElementConfigurer;
import org.eclipse.papyrus.uml.domain.services.create.IDomainBasedEdgeCreator;
import org.eclipse.papyrus.uml.domain.services.edges.ElementDomainBasedEdgeInitializer;
import org.eclipse.papyrus.uml.domain.services.edges.diagrams.ComponentDomainBasedEdgeContainerProvider;
import org.eclipse.papyrus.uml.domain.services.modify.ElementFeatureModifier;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramElementHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.diagrams.renderer.DiagramRenderingCache;
import org.eclipse.sirius.components.representations.Element;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.stereotype.Service;

/**
 * Service for the "Component" diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class ComponentDiagramService extends AbstractDiagramService {

    /**
     * Logger used to report errors and warnings to the user.
     */
    private ILogger logger;

    /**
     * Factory used to create UML elements.
     */
    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * Constructor.
     *
     * @param objectService
     *            service used to retrieve semantic object from a Diagram node
     * @param diagramNavigationService
     *            helper that must introspect the current diagram's structure and its description
     * @param diagramOperationsService
     *            helper that must modify the current diagram, most notably create or delete views for unsynchronized
     *            elements
     * @param editableChecker
     *            Object that check if an element can be edited
     * @param viewDiagramService
     *            Service used to navigate in DiagramDescription
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    public ComponentDiagramService(IObjectService objectService, IDiagramNavigationService diagramNavigationService, IDiagramOperationsService diagramOperationsService,
            IEditableChecker editableChecker, IViewDiagramDescriptionService viewDiagramService, ILogger logger) {
        super(objectService, diagramNavigationService, diagramOperationsService, editableChecker, viewDiagramService, logger);
        this.logger = logger;
    }

    @Override
    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getObjectService(), this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext, capturedNodeDescriptions);
        IWebExternalSourceToRepresentationDropBehaviorProvider dropProvider = new ComponentSemanticDropBehaviorProvider(editionContext, createViewHelper, this.getObjectService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    @Override
    protected IWebInternalSourceToRepresentationDropBehaviorProvider buildGraphicalDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getObjectService(), this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext, capturedNodeDescriptions);
        IWebInternalSourceToRepresentationDropBehaviorProvider dropProvider = new ComponentGraphicalDropBehaviorProvider(editionContext, createViewHelper, this.getObjectService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    @Override
    protected IDomainBasedEdgeCreator buildDomainBasedEdgeCreator(EObject source) {
        ElementBasedEdgeCreator baseEdgeCreator = new ElementBasedEdgeCreator(//
                ComponentDomainBasedEdgeContainerProvider.buildDefault(this.getEditableChecker()), //
                new ElementDomainBasedEdgeInitializer(), //
                new ElementConfigurer(), //
                new ElementFeatureModifier(this.getECrossReferenceAdapter(source), this.getEditableChecker()));
        return baseEdgeCreator;
    }

    /**
     * Service used to check if a {@link Property} can be created under the specified parent.
     *
     * @param parent
     *            the container that should contains the new object to create
     * @return {@code true} if the object can be created, {@code false} otherwise
     */
    public boolean canCreatePropertyIntoParentCPD(EObject parent) {
        boolean canCreate = false;
        if (parent instanceof Property property && !(parent instanceof Port)) {
            canCreate = property.getType() != null && (property.getType() instanceof StructuredClassifier);
        } else {
            canCreate = parent instanceof StructuredClassifier;
        }
        return canCreate;
    }

    /**
     * A service to create a {@link Port} in the given {@code container}.
     *
     * @param container
     *            the future container
     * @param targetView
     *            the selected Graphical container
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescription
     *            the {@link NodeDescription}s
     * @return the created {@link Port}
     */
    public EObject createPortCPD(EObject container, org.eclipse.sirius.components.diagrams.Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPropertyAndSubType(container, this.umlPackage.getPort().getName(), targetView, diagramContext, capturedNodeDescriptions);
    }

    /**
     * A service to create a {@link Port} in the given {@code container}.
     *
     * @param container
     *            the future container
     * @param targetView
     *            the selected Graphical container
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescription
     *            the {@link NodeDescription}s
     * @return the created {@link Port}
     */
    public EObject createPortInHolderCPD(EObject container, org.eclipse.sirius.components.diagrams.Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPropertyAndSubType(container, this.umlPackage.getPort().getName(), this.getParentNode(targetView, diagramContext.getDiagram()), diagramContext, capturedNodeDescriptions);
    }

    /**
     * A service to create a {@link Property} in the given {@code container}.
     *
     * @param container
     *            the future container
     * @param targetView
     *            the selected Graphical container
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescription
     *            the {@link NodeDescription}s
     * @return the created {@link Property}
     */
    public EObject createPropertyCPD(EObject container, org.eclipse.sirius.components.diagrams.Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPropertyAndSubType(container, this.umlPackage.getProperty().getName(), targetView, diagramContext, capturedNodeDescriptions);
    }

    /**
     * A service to create a {@link Property} or one of its sub-type in the given {@code container}.
     *
     * @param container
     *            the future container
     * @param propertyClassName
     *            name of the kind of {@link Property} to create
     * @param targetView
     *            the selected Graphical container
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescription
     *            the {@link NodeDescription}s
     * @return the created {@link Property}
     */
    private EObject createPropertyAndSubType(EObject container, String propertyClassName, org.eclipse.sirius.components.diagrams.Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        EObject newProperty = null;
        String containmentFeatureName = null;
        if (container instanceof Property propertyContainer) {
            Type type = propertyContainer.getType();
            if (type instanceof StructuredClassifier typeContainer) {
                containmentFeatureName = this.umlPackage.getStructuredClassifier_OwnedAttribute().getName();
                newProperty = this.create(typeContainer, propertyClassName, containmentFeatureName, targetView, diagramContext, capturedNodeDescriptions);
            }
        } else if (container instanceof StructuredClassifier) {
            containmentFeatureName = this.umlPackage.getStructuredClassifier_OwnedAttribute().getName();
            newProperty = this.create(container, propertyClassName, containmentFeatureName, targetView, diagramContext, capturedNodeDescriptions);
        }
        if (newProperty == null) {
            // Workaround for https://github.com/eclipse-sirius/sirius-components/issues/1343
            return FAILURE_OBJECT;
        }
        return newProperty;
    }

    /**
     * Provides {@link Port} candidates.
     *
     * @param container
     *            the current container in which looking for the Properties
     * @return the {@link Port} list
     */
    public List<? extends Port> getPortCandidatesCPD(EObject container) {
        List<? extends Port> portCandidates = List.of();
        if (container instanceof Property propertyContainer) {
            Type type = propertyContainer.getType();
            if (type instanceof StructuredClassifier typeContainer) {
                portCandidates = typeContainer.getAllAttributes().stream() //
                        .filter(a -> a.eClass() == this.umlPackage.getPort()) //
                        .map(e -> (Port) e)//
                        .toList();
            }
        } else if (container instanceof StructuredClassifier structuredClassifierContainer) {
            portCandidates = structuredClassifierContainer.getAllAttributes().stream() //
                    .filter(a -> a.eClass() == this.umlPackage.getPort()) //
                    .map(e -> (Port) e)//
                    .toList();
        }
        return portCandidates;
    }

    /**
     * Provides {@link Property} candidates.
     *
     * @param container
     *            the current container in which looking for the Properties
     * @return the {@link Property} list
     */
    public List<? extends Property> getPropertyCandidatesCPD(EObject container) {
        List<? extends Property> propertyCandidates = List.of();
        if (container instanceof Property propertyContainer) {
            Type type = propertyContainer.getType();
            if (type instanceof StructuredClassifier typeContainer) {
                propertyCandidates = typeContainer.getAllAttributes().stream() //
                        .filter(a -> a.eClass() == this.umlPackage.getProperty()) //
                        .toList();
            }
        } else if (container instanceof StructuredClassifier structuredClassifierContainer) {
            propertyCandidates = structuredClassifierContainer.getAllAttributes().stream() //
                    .filter(a -> a.eClass() == this.umlPackage.getProperty()) //
                    .toList();
        }
        return propertyCandidates;
    }

    /**
     * Connector use both {@link ConnectorEnd#getRole()} and {@link ConnectorEnd#getPartWithPort()} information to pick
     * a source and a target. This method prevents displaying invalid connectors.
     *
     * @param connector
     *            the connector to display or not
     * @param semanticSource
     *            the semantic source of the edge
     * @param semanticTarget
     *            the semantic target of the edge
     * @param visualSource
     *            the visual source of the edge candidate
     * @param visualTarget
     *            the visual target of the edge candidate
     * @param cache
     *            the {@link DiagramRenderingCache}
     * @param editableChecker
     *            Object that check if an element can be edited
     * @return {@code true} if the edge should be displayed, {@code false} otherwise
     */
    public boolean shouldDisplayConnector(Connector connector, EObject semanticSource, EObject semanticTarget, Element visualSource, Element visualTarget, DiagramRenderingCache cache,
            IEditingContext editingContext) {

        // compute sourceEnd
        ConnectorEnd sourceEnd = connector.getEnds().get(0);
        ConnectorEnd targetEnd = connector.getEnds().get(1);
        boolean matchPartWithPort = this.matchPartWithPort(sourceEnd, visualSource, cache, editingContext) && this.matchPartWithPort(targetEnd, visualTarget, cache, editingContext);

        if (matchPartWithPort) {
            return this.getCommonVisualAncestor(visualSource, visualTarget, cache, editingContext).map(ancestor -> ancestor == connector.eContainer()).orElse(false);
        } else {
            return false;
        }
    }

    private Optional<Object> getCommonVisualAncestor(Element visualSource, Element visualTarget, DiagramRenderingCache cache, IEditingContext editinContext) {
        return new DiagramElementHelper(visualSource).getCommonAncestor(new DiagramElementHelper(visualTarget), cache)//
                .filter(element -> element.getId().isPresent())//
                .flatMap(ancestor -> ancestor.getElementTarget(this.getObjectService(), editinContext));
    }

    /**
     * Check that the connector end match the given visual element. That is to say that the target role is matched and
     * the {@link ConnectorEnd#getPartWithPort()} feature also matches.
     *
     * @param end
     *            the given {@link ConnectorEnd}
     * @param visualElement
     *            the visual element to match
     * @param cache
     *            the {@link DiagramRenderingCache}
     * @param editingContext
     *            the {@link EditingContext}
     * @return {@code true} if the visual element matches the given {@link ConnectorEnd}, {@code false} otherwise
     */
    private boolean matchPartWithPort(ConnectorEnd end, Element visualElement, DiagramRenderingCache cache, IEditingContext editingContext) {
        Property partWithPortSource = end.getPartWithPort();
        boolean shouldDiplayConnector = true;
        if (partWithPortSource != null) {
            // connector source is a Port on a Property
            DiagramElementHelper visualSourceHelper = new DiagramElementHelper(visualElement);
            Optional<Object> target = visualSourceHelper.getElementTarget(this.getObjectService(), editingContext);

            if (target.isPresent() && target.get() instanceof Port) {
                shouldDiplayConnector = visualSourceHelper.getParent(cache)//
                        .flatMap(parent -> parent.getElementTarget(this.getObjectService(), editingContext))//
                        .map(sem -> sem == partWithPortSource).orElse(false);
            } else {
                shouldDiplayConnector = false;
            }
        }
        return shouldDiplayConnector;
    }

}
