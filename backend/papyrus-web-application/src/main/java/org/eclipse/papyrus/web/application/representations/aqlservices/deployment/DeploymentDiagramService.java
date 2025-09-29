/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.application.representations.aqlservices.deployment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.CommunicationPath;
import org.eclipse.uml2.uml.DeploymentSpecification;
import org.eclipse.uml2.uml.Device;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionEnvironment;
import org.eclipse.uml2.uml.Manifestation;
import org.eclipse.uml2.uml.Node;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.impl.ArtifactImpl;
import org.eclipse.uml2.uml.internal.impl.NodeImpl;
import org.springframework.stereotype.Service;

/**
 * Service for the "Deployment" diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class DeploymentDiagramService extends AbstractDiagramService {

    /**
     * Logger used to report errors and warnings to the user.
     */
    private final ILogger logger;

    /**
     * Constructor.
     *
     * @param identityService
     *            the service in charge of getting the identity of an object
     * @param labelService
     *            the service in charge of getting labels and images of an object
     * @param objectSearchService
     *            the service in charge of getting an object from its id
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
    //CHECKSTYLE:OFF Injected parameters
    public DeploymentDiagramService(IIdentityService identityService, ILabelService labelService,
            IObjectSearchService objectSearchService, IDiagramNavigationService diagramNavigationService,
            IDiagramOperationsService diagramOperationsService,
            IEditableChecker editableChecker, IViewDiagramDescriptionService viewDiagramService, ILogger logger) {
        //CHECKSTYLE:ON Injected parameters
        super(identityService, labelService, objectSearchService, diagramNavigationService, diagramOperationsService,
                editableChecker, viewDiagramService, logger);
        this.logger = logger;
    }

    @Override
    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext,
                capturedNodeDescriptions);
        IWebExternalSourceToRepresentationDropBehaviorProvider dropProvider = new DeploymentSemanticDropBehaviorProvider(
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
        IWebInternalSourceToRepresentationDropBehaviorProvider dropProvider = new DeploymentGraphicalDropBehaviorProvider(
                editionContext, createViewHelper, this.getObjectSearchService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    /**
     * A service to create an {@link Artifact} in the given {@code container}.
     *
     * @param container
     *            the future container.
     * @param type
     *            the type of the {@link Artifact} to create: {@code Artifact} {@code DeploymentSpecification}
     * @param targetView
     *            the selected Graphical container.
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the created {@link Artifact}.
     */
    public EObject createArtifactDD(EObject container, String type, org.eclipse.sirius.components.diagrams.Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        EObject newArtifact = null;
        Optional<EReference> optionalContainmentFeature = this.getArtifactContainmentFeature((Element) container);
        if (optionalContainmentFeature.isPresent()) {
            newArtifact = this.create(container, type, optionalContainmentFeature.get().getName(), targetView, diagramContext, capturedNodeDescriptions);
        }
        return newArtifact;
    }

    /**
     * Create a {@link Manifestation}.
     *
     * @param source
     *            the semantic source
     * @param target
     *            the semantic target
     * @param sourceNode
     *            the source node
     * @param targetNode
     *            the target node
     * @param editingContext
     *            the current {@link IEditingContext}
     * @param diagramContext
     *            the current {@link IDiagramContext}
     * @return a new {@link Manifestation}
     */
    public EObject createManifestationDD(EObject source, EObject target, org.eclipse.sirius.components.diagrams.Node sourceNode, org.eclipse.sirius.components.diagrams.Node targetNode,
            IEditingContext editingContext, IDiagramContext diagramContext) {
        EObject newEdge = null;
        if (source instanceof Artifact) {
            newEdge = this.createDomainBasedEdge(source, target, UMLPackage.eINSTANCE.getManifestation().getName(), UMLPackage.eINSTANCE.getArtifact_Manifestation().getName(), sourceNode, targetNode,
                    editingContext, diagramContext);
        } else if (source instanceof PackageableElement) {
            newEdge = this.createDomainBasedEdge(source, target, UMLPackage.eINSTANCE.getManifestation().getName(), UMLPackage.eINSTANCE.getPackage_PackagedElement().getName(), sourceNode, targetNode,
                    editingContext, diagramContext);
        }
        return newEdge;
    }

    /**
     * A service to create a {@link Node} in the given {@code container}.
     *
     * @param container
     *            the future container.
     * @param type
     *            the type of the {@link Node} to create: {@code Node}, {@code Device}, {@code ExecutionEnvironment}
     * @param targetView
     *            the selected Graphical container.
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the created {@link Node}.
     */
    public EObject createNodeDD(Element container, String type, org.eclipse.sirius.components.diagrams.Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        EObject newNode = null;
        Optional<EReference> optionalContainmentFeature = this.getNodeContainmentFeature(container);
        if (optionalContainmentFeature.isPresent()) {
            newNode = this.create(container, type, optionalContainmentFeature.get().getName(), targetView, diagramContext, capturedNodeDescriptions);
        }
        return newNode;
    }

    /**
     * Provides {@link Artifact} candidates.
     *
     * @param container
     *            the current container in which looking for the Artifacts.
     * @return the {@link Artifact} list.
     */
    public List<? extends Artifact> getArtifactCandidatesDD(Element container) {
        Optional<EReference> optionalContainmentFeature = this.getArtifactContainmentFeature(container);
        if (optionalContainmentFeature.isPresent()) {
            List<? extends Artifact> artifactCandidates = this.retrieveCandidates(Artifact.class, container, optionalContainmentFeature.get());
            return artifactCandidates.stream().filter(a -> a.getClass().equals(ArtifactImpl.class)).toList();
        }
        return List.of();
    }

    /**
     * Get all {@link CommunicationPath} found in the context.
     *
     * @param semanticContext
     *            the context in which we are looking for CommunicationPaths
     * @return all {@link CommunicationPath} available in the context
     */
    public Collection<CommunicationPath> getCommunicationPathCandidatesDD(final EObject semanticContext) {
        if (semanticContext instanceof Package) {
            final Package pack = (Package) semanticContext;
            return this.getAllCommunicationPaths(pack);
        }
        return Collections.emptyList();
    }

    /**
     * Provides {@link DeploymentSpecification} candidates.
     *
     * @param container
     *            the current container in which looking for the DeploymentSpecifications.
     * @return the {@link DeploymentSpecification} list.
     */
    public List<? extends DeploymentSpecification> getDeploymentSpecificationCandidatesDD(Element container) {
        Optional<EReference> optionalContainmentFeature = this.getArtifactContainmentFeature(container);
        if (optionalContainmentFeature.isPresent()) {
            return this.retrieveCandidates(DeploymentSpecification.class, container, optionalContainmentFeature.get());
        }
        return List.of();
    }

    /**
     * Provides {@link Device} candidates.
     *
     * @param container
     *            the current container in which looking for the Devices.
     * @return the {@link Device} list.
     */
    public List<? extends Device> getDeviceCandidatesDD(Element container) {
        Optional<EReference> optionalContainmentFeature = this.getNodeContainmentFeature(container);
        if (optionalContainmentFeature.isPresent()) {
            return this.retrieveCandidates(Device.class, container, optionalContainmentFeature.get());
        }
        return List.of();
    }

    /**
     * Provides {@link ExecutionEnvironment} candidates.
     *
     * @param container
     *            the current container in which looking for the ExecutionEnvironments.
     * @return the {@link ExecutionEnvironment} list.
     */
    public List<? extends ExecutionEnvironment> getExecutionEnvironmentCandidatesDD(Element container) {
        Optional<EReference> optionalContainmentFeature = this.getNodeContainmentFeature(container);
        if (optionalContainmentFeature.isPresent()) {
            return this.retrieveCandidates(ExecutionEnvironment.class, container, optionalContainmentFeature.get());
        }
        return List.of();
    }

    /**
     * Provides {@link Node} candidates.
     *
     * @param container
     *            the current container in which looking for the Nodes.
     * @return the {@link Node} list.
     */
    public List<? extends Node> getNodeCandidatesDD(Element container) {
        Optional<EReference> optionalContainmentFeature = this.getNodeContainmentFeature(container);
        if (optionalContainmentFeature.isPresent()) {
            List<? extends Node> nodeCandidates = this.retrieveCandidates(Node.class, container, optionalContainmentFeature.get());
            return nodeCandidates.stream().filter(a -> a.getClass().equals(NodeImpl.class)).toList();
        }
        return List.of();
    }

    private Optional<EReference> getNodeContainmentFeature(Element container) {
        Optional<EReference> reference;
        if (container instanceof Package pkg) {
            reference = Optional.of(UMLPackage.eINSTANCE.getPackage_PackagedElement());
        } else if (container instanceof Node node) {
            reference = Optional.of(UMLPackage.eINSTANCE.getNode_NestedNode());
        } else {
            reference = Optional.empty();
        }
        return reference;
    }

    private Optional<EReference> getArtifactContainmentFeature(Element container) {
        Optional<EReference> reference;
        if (container instanceof Package pkg) {
            reference = Optional.of(UMLPackage.eINSTANCE.getPackage_PackagedElement());
        } else if (container instanceof org.eclipse.uml2.uml.Class clazz) {
            reference = Optional.of(UMLPackage.eINSTANCE.getClass_NestedClassifier());
        } else if (container instanceof Artifact artifact) {
            reference = Optional.of(UMLPackage.eINSTANCE.getArtifact_NestedArtifact());
        } else {
            reference = Optional.empty();
        }
        return reference;
    }

    private <T> List<? extends T> retrieveCandidates(Class<? extends T> type, Element container, EReference containmentFeature) {
        Stream<?> streamOfCandidates = Optional.ofNullable(container.eGet(containmentFeature)).filter(List.class::isInstance)//
                .map(List.class::cast)//
                .stream()//
                .flatMap(List::stream);
        return streamOfCandidates.filter(type::isInstance)//
                .map(type::cast)//
                .toList();
    }

    private Collection<CommunicationPath> getAllCommunicationPaths(final Package pack) {
        final Collection<CommunicationPath> communicationPaths = new HashSet<>();
        final Iterator<PackageableElement> iter = pack.getPackagedElements().iterator();
        while (iter.hasNext()) {
            final PackageableElement current = iter.next();
            if (current instanceof Package) {
                communicationPaths.addAll(this.getAllCommunicationPaths((Package) current));
            }
            if (current instanceof CommunicationPath) {
                communicationPaths.add((CommunicationPath) current);
            }
        }
        return communicationPaths;
    }

}
