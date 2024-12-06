/*******************************************************************************
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.utils;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.papyrus.uml.domain.services.EMFUtils.allContainedObjectOfType;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.application.representations.PapyrusRepresentationDescriptionRegistry;
import org.eclipse.papyrus.web.sirius.contributions.DiagramDescriptionVisitor;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramBuilderService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher.BorderNodeStatus;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.emf.diagram.IDiagramIdProvider;
import org.springframework.data.util.Pair;

import graphql.AssertException;

/**
 * Helper that helps builds mocked {@link Diagram} with {@link Node} and {@link Edge}. It also offers way to check if
 * some request have been added (for example creation request).
 *
 * @author Arthur Daussy
 */
public class DiagramTestHelper {

    private IDiagramIdProvider idProvider;

    private Map<String, NodeDescription> nodeIdToDescriptions;

    private Map<String, org.eclipse.sirius.components.diagrams.description.EdgeDescription> edgeIdToDescriptions;

    private org.eclipse.sirius.components.diagrams.description.DiagramDescription convertedDiagramDescription;

    private Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> converterNodes;

    private EObject diagramOwner;

    private Diagram diagram;

    private IEditingContext editingContext;

    private final IObjectService objectService;

    private IDiagramBuilderService diagramBuilderService;

    private DiagramDescription diagramDescription;

    private PapyrusRepresentationDescriptionRegistry viewRegistry;

    private IDiagramOperationsService diagramOpService;

    private IDiagramNavigationService diagramNavigationService;

    private IViewDiagramDescriptionService viewDiagramDescriptionService;

    // CHECKSTYLE:OFF FOR now
    public DiagramTestHelper(IEditingContext editingContext, IObjectService objectService, PapyrusRepresentationDescriptionRegistry viewRegistry, IDiagramBuilderService diagramBuilderService,
            IDiagramOperationsService diagramOpService, IDiagramNavigationService diagramNavigationService, IViewDiagramDescriptionService viewDiagramDescriptionService,
            IDiagramIdProvider idProvider) {
        super();
        this.editingContext = editingContext;
        this.objectService = objectService;
        this.viewRegistry = viewRegistry;
        this.diagramBuilderService = diagramBuilderService;
        this.diagramOpService = diagramOpService;
        this.diagramNavigationService = diagramNavigationService;
        this.viewDiagramDescriptionService = viewDiagramDescriptionService;
        this.idProvider = idProvider;
    }
    // CHECKSTYLE:ON FOR now

    /**
     * Initializes the diagram.
     *
     * @param newOwner
     *            the semantic owner
     * @param diagramName
     *            the name of the diagram
     */
    public void init(EObject newOwner, String diagramName) {
        this.diagramOwner = newOwner;
        this.diagramDescription = this.viewRegistry.getViewDiagramDescriptionByName(diagramName).orElseThrow();
        this.convertedDiagramDescription = (org.eclipse.sirius.components.diagrams.description.DiagramDescription) this.viewRegistry.getApiDiagramDescriptionByName(diagramName).orElseThrow();

        this.nodeIdToDescriptions = new HashMap<>();
        this.edgeIdToDescriptions = new HashMap<>();
        DiagramDescriptionVisitor diagramDescriptionVisitor = new DiagramDescriptionVisitor(this.convertedDiagramDescription);
        diagramDescriptionVisitor.visitNodes(n -> this.nodeIdToDescriptions.put(n.getId(), n));
        diagramDescriptionVisitor.visitEdges(e -> this.edgeIdToDescriptions.put(e.getId(), e));
        this.converterNodes = this.viewRegistry.getConvertedNode(diagramName);

        this.diagram = this.diagramBuilderService.createDiagram(this.editingContext, d -> this.matchDiagramLabel(diagramName, d), this.diagramOwner, diagramName).get();
    }

    private boolean matchDiagramLabel(String diagramName, org.eclipse.sirius.components.diagrams.description.DiagramDescription d) {
        String label = d.getLabel();
        return label != null && label.equals(diagramName);
    }

    /**
     * Modifies the diagram.
     *
     * @param <T>
     *            the type of the returned object
     * @param updater
     *            an diagram updater. It will be given the diagram context
     * @return a value return by the updater
     */
    public <T> T modify(Function<IDiagramContext, T> updater) {
        Optional<Pair<Diagram, T>> result = this.diagramBuilderService.updateDiagramAndGet(this.diagram, this.editingContext, updater);
        result.ifPresent(p -> {
            this.diagram = p.getFirst();
        });
        return result.map(Pair::getSecond).orElse(null);
    }

    /**
     * Forces the diagram refresh.
     */
    public void refresh() {
        this.diagram = this.diagramBuilderService.refreshDiagram(this.diagram, this.editingContext).get();
    }

    /**
     * Modifies the diagram.
     *
     * @param updater
     *            an diagram updater. It will be given the diagram context
     */
    public void modify(Consumer<IDiagramContext> updater) {
        this.diagram = this.diagramBuilderService.updateDiagram(this.diagram, this.editingContext, updater).get();
    }

    public Diagram getDiagram() {
        return this.diagram;
    }

    public EObject getDiagramOwner() {
        return this.diagramOwner;
    }

    public Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> getConvertedNodes() {
        return this.converterNodes;
    }

    private NodeDescription getNodeDescriptionById(String id) {
        return this.nodeIdToDescriptions.get(id);
    }

    /**
     * Creates a new Node at the root of the diagram.
     *
     * @param nodeDescriptionName
     *            the name of the description view
     * @param semanticElement
     *            the semantic element
     * @return the newly created node or fails if the creating failed
     */
    public Node createNodeInDiagram(String nodeDescriptionName, EObject semanticElement) {
        return this.createNode(nodeDescriptionName, semanticElement, Optional.empty());
    }

    /**
     * Creates a new Node in a the given parent.
     *
     * @param nodeDescriptionName
     *            the name of the description view
     * @param semanticElement
     *            the semantic element
     * @param parentNode
     *            the parent node
     * @return the newly created node or fails if the creating failed
     */
    public Node createNodeInParent(String nodeDescriptionName, EObject semanticElement, Node parentNode) {
        return this.createNode(nodeDescriptionName, semanticElement, Optional.of(parentNode));
    }

    private Node createNode(String nodeDescriptionName, EObject semanticElement, Optional<Node> parent) {

        org.eclipse.sirius.components.view.diagram.NodeDescription viewNodeDescription = this.viewDiagramDescriptionService.getNodeDescriptionByName(this.diagramDescription, nodeDescriptionName)
                .orElseThrow(() -> new NoSuchElementException("Unable to find node description with name " + nodeDescriptionName));

        var isBorderedNode = viewNodeDescription.eContainingFeature() == DiagramPackage.eINSTANCE.getNodeDescription_BorderNodesDescriptions();
        final NodeContainmentKind containmentKind;
        if (isBorderedNode) {
            containmentKind = NodeContainmentKind.BORDER_NODE;
        } else {
            containmentKind = NodeContainmentKind.CHILD_NODE;
        }

        NodeDescription nodeDescriptionByViewName = this.getConvertedNodeDescription(viewNodeDescription);

        this.modify(context -> {
            this.diagramOpService.createView(context, semanticElement, parent, nodeDescriptionByViewName, containmentKind);
        });

        Predicate<Object> isMatchingSemantic = this.buildSemanticPredicate(semanticElement);
        Predicate<Node> nodeDescriptionPredicate = this.buildNodeDescriptionNamePredicate(nodeDescriptionName);
        DiagramNavigator navigator = this.buildDiagramNavigator();
        Predicate<Node> parentNodePredicate = this.buildParentNodePredicate(navigator, parent.orElse(null));
        return this.getUniqueMatch(navigator, isMatchingSemantic, nodeDescriptionPredicate.and(parentNodePredicate));

    }

    /**
     * Find a NodeDescription if it exists.
     *
     * @param nodeDescriptionName
     *            the name of the description
     * @return the NodeDescription as optional
     */
    public Optional<NodeDescription> getOptionalNodeDescriptionByName(String nodeDescriptionName) {
        return this.viewDiagramDescriptionService.getNodeDescriptionByName(this.diagramDescription, nodeDescriptionName)//
                .map(this::getConvertedNodeDescription);
    }

    private NodeDescription getNodeDescriptionByName(String nodeDescriptionName) {
        return this.viewDiagramDescriptionService.getNodeDescriptionByName(this.diagramDescription, nodeDescriptionName)//
                .map(this::getConvertedNodeDescription)//
                .orElseThrow(() -> new NoSuchElementException("Unable to find node description with name " + nodeDescriptionName));
    }

    private Predicate<Node> buildNodeDescriptionNamePredicate(String descriptionName) {
        NodeDescription pDesc = this.getNodeDescriptionByName(descriptionName);
        if (pDesc == null) {
            throw new IllegalArgumentException("Unable to get description for name " + descriptionName);
        } else {
            return n -> pDesc.getId().equals(n.getDescriptionId());
        }
    }

    private Predicate<Node> buildParentNodePredicate(DiagramNavigator navigator, Node parent) {
        if (parent != null) {
            String parentId = parent.getId();
            return n -> navigator.getParentNode(n).map(p -> p.getId().equals(parentId)).orElse(false);
        } else {
            return n -> navigator.getParentNode(n).isEmpty();
        }
    }

    /**
     * Gets the semantic element of a node.
     *
     * @param node
     *            a Node
     * @return a semantic element (or <b>fails</b>)
     */
    public EObject getSemanticElement(Node node) {
        Optional<Object> optSemanticOwner = this.objectService.getObject(this.editingContext, node.getTargetObjectId());
        if (optSemanticOwner.isEmpty()) {
            fail("Unable to find semantic element of " + node);
        }
        EObject semanticSibling = (EObject) optSemanticOwner.get();
        return semanticSibling;
    }

    /**
     * Gets the semantic element of an edge.
     *
     * @param edge
     *            an Edge
     * @return a semantic element (or <b>fails</b>)
     */
    public EObject getSemanticElement(Edge edge) {
        Optional<Object> optSemanticOwner = this.objectService.getObject(this.editingContext, edge.getTargetObjectId());
        if (optSemanticOwner.isEmpty()) {
            fail("Unable to find semantic element of " + edge);
        }
        EObject semanticSibling = (EObject) optSemanticOwner.get();
        return semanticSibling;
    }

    private Predicate<Node> buildNodeIDPredicate(String nodeId) {
        return n -> nodeId.equals(n.getId());
    }

    private Predicate<Object> buildSemanticPredicate(EObject semanticTarget) {
        return s -> s == semanticTarget;
    }

    /**
     * Gets all the edges matching the given criteria.
     *
     * @param optExpectedSemanticTargetId
     *            an optional semantic element id of the matching edge (for domain based edge)
     * @param optDescriptionId
     *            an optional id of the {@link Edge}
     * @param optExpectedSourceNodeId
     *            an optional id of a source {@link Node}
     * @param optExpectedTargetNodeId
     *            an optional id of a target {@link Node}
     * @return a list of {@link Edge}s
     */
    public List<Edge> getAllMatchingEdges(Optional<String> optExpectedSemanticTargetId, Optional<String> optDescriptionId, Optional<String> optExpectedSourceNodeId,
            Optional<String> optExpectedTargetNodeId) {
        return this.diagram.getEdges().stream().filter(e -> this.isMatchingEdge(e, optExpectedSemanticTargetId, optDescriptionId, optExpectedSourceNodeId, optExpectedTargetNodeId)).collect(toList());

    }

    private Edge searchOneMatchingEdge(Optional<String> expectedSemanticTargetId, Optional<String> descriptionId, Optional<String> expectedSourceNodeId, Optional<String> expectedTargetNodeId) {
        List<Edge> edges = this.getAllMatchingEdges(expectedSemanticTargetId, descriptionId, expectedSourceNodeId, expectedTargetNodeId);
        if (edges.isEmpty()) {
            return null;
        } else {
            return edges.get(0);
        }
    }

    private boolean isMatchingEdge(Edge edge, Optional<String> expectedSemanticTargetId, Optional<String> descriptionId, Optional<String> expectedSourceNodeId, Optional<String> expectedTargetNodeId) {
        final boolean isMatchingSemantic;
        if (expectedSemanticTargetId.isPresent()) {
            isMatchingSemantic = Objects.equals(expectedSemanticTargetId.get(), edge.getTargetObjectId());
        } else {
            isMatchingSemantic = true;
        }
        final boolean isMatchingSourceNode;
        if (expectedSourceNodeId.isPresent()) {
            isMatchingSourceNode = Objects.equals(expectedSourceNodeId.get(), edge.getSourceId());
        } else {
            isMatchingSourceNode = true;
        }

        final boolean isMatchingTargetNode;
        if (expectedTargetNodeId.isPresent()) {
            isMatchingTargetNode = Objects.equals(expectedTargetNodeId.get(), edge.getTargetId());
        } else {
            isMatchingTargetNode = true;
        }

        boolean isMatchingDescription;
        if (descriptionId.isPresent()) {
            isMatchingDescription = Objects.equals(descriptionId.get(), edge.getDescriptionId());
        } else {
            isMatchingDescription = true;
        }
        return isMatchingSemantic && isMatchingSourceNode && isMatchingTargetNode && isMatchingDescription;
    }

    private NodeDescription getConvertedNodeDescription(org.eclipse.sirius.components.view.diagram.NodeDescription viewDescription) {
        return this.getNodeDescriptionById(this.idProvider.getId(viewDescription));
    }

    private EdgeDescription getViewEdgeDescriptionByName(String name) {
        List<EdgeDescription> matchingElements = allContainedObjectOfType(this.diagramDescription, EdgeDescription.class)//
                .filter(e -> name.equals(e.getName())).collect(toList());

        if (matchingElements.isEmpty()) {
            throw new AssertException("No node desciption with name " + name);
        } else if (matchingElements.size() > 1) {
            throw new AssertException("More than one node description with same  " + name);
        } else {
            return matchingElements.get(0);
        }
    }

    /**
     * Checks that the diagram contains a {@link Node} described by
     * {@link org.eclipse.sirius.components.view.NodeDescription} targeting the given semantic element.
     *
     * @param expectedDodeDescriptontionName
     *            the name of the expected {@link org.eclipse.sirius.components.view.NodeDescription}
     * @param expectedSemanticElement
     *            the semantic target of the node
     * @return the matched node
     */
    public Node assertGetUniqueRootNode(String expectedDodeDescriptontionName, EObject expectedSemanticElement) {
        Predicate<Object> semanticPredicate = this.buildSemanticPredicate(expectedSemanticElement);
        Predicate<Node> nodePredicate = this.buildNodeDescriptionNamePredicate(expectedDodeDescriptontionName);
        DiagramNavigator navigator = this.buildDiagramNavigator();
        Predicate<Node> parentNodePredicate = this.buildParentNodePredicate(navigator, null);
        return this.getUniqueMatch(navigator, semanticPredicate, parentNodePredicate.and(nodePredicate));
    }

    private Node getUniqueMatch(DiagramNavigator navigator, Predicate<Object> semanticPredicate, Predicate<Node> nodePredicate) {
        List<Node> matchingNodes = navigator.getMatchingNodes(this.editingContext, NodeMatcher.buildSemanticAndNodeMatcher(BorderNodeStatus.BOTH, semanticPredicate, nodePredicate));

        if (matchingNodes.size() == 0) {
            fail("Unable to find a node");

        } else if (matchingNodes.size() > 1) {
            String matchinString = matchingNodes.stream()//
                    .map(n -> this.objectService.getObject(this.editingContext, n.getTargetObjectId()))//
                    .map(s -> this.objectService.getLabel(s))//
                    .collect(joining());
            fail(MessageFormat.format("More than one node matching predicates :{0} ", matchinString));

        }
        return matchingNodes.get(0);
    }

    /**
     * Checks that a domain based edge exist in the diagram matching the given criteria.
     *
     * @param descriptionName
     *            the name of the {@link org.eclipse.sirius.components.view.EdgeDescription}
     * @param newElement
     *            the target semantic element
     * @param source
     *            the source node
     * @param target
     *            the target node
     * @return the matching edge
     */
    public Edge assertGetExistDomainBasedEdge(String descriptionName, EObject newElement, Node source, Node target) {

        Edge matchingEdge = this.getMatchingEdge(Optional.of(descriptionName), Optional.of(this.objectService.getId(newElement)), Optional.of(source.getId()), Optional.of(target.getId()));
        if (matchingEdge == null) {
            fail("No domain base edge given the matching criteria");
        }

        return matchingEdge;
    }

    /**
     * Get an edge matching the given criteria.
     *
     * @param optDescriptionName
     *            an optional name for the view description
     * @param optSemanticId
     *            an optional semantic id
     * @param optSourceNodeId
     *            an optional source node id
     * @param targetNodeId
     *            an optional target node id
     * @return an Edge or <code>null</code> if none match
     */
    public Edge getMatchingEdge(Optional<String> optDescriptionName, Optional<String> optSemanticId, Optional<String> optSourceNodeId, Optional<String> targetNodeId) {

        Optional<String> pDescId = optDescriptionName.map(name -> {
            EdgeDescription description = this.getViewEdgeDescriptionByName(name);
            return this.edgeIdToDescriptions.get(this.idProvider.getId(description)).getId();
        });

        Edge matchingEdge = this.searchOneMatchingEdge(optSemanticId, pDescId, optSourceNodeId, targetNodeId);
        return matchingEdge;
    }

    /**
     * Gets the list of all edges matching the given criteria.
     *
     * @param optDescriptionName
     *            an optional name of the view description
     * @param optSemanticId
     *            an optional semantic id
     * @param optSourceNodeId
     *            an optional source node id
     * @param optTargetNodeId
     *            an optional target id
     * @return a list of {@link Edge}
     */
    public List<Edge> getMatchingEdges(Optional<String> optDescriptionName, Optional<String> optSemanticId, Optional<String> optSourceNodeId, Optional<String> optTargetNodeId) {

        Optional<String> pDescId = optDescriptionName.map(name -> {
            EdgeDescription description = this.getViewEdgeDescriptionByName(name);
            return this.edgeIdToDescriptions.get(this.idProvider.getId(description)).getId();
        });

        return this.getAllMatchingEdges(optSemanticId, pDescId, optSourceNodeId, optTargetNodeId);
    }

    private Boolean matchDescriptionName(String expectedDescName, Node testedNode) {
        DiagramNavigator navigator = this.buildDiagramNavigator();
        return navigator.getDescription(testedNode).map(descMatch -> descMatch.getName().equals(expectedDescName)).orElse(false);
    }

    /**
     * Gets a parent node of the given node.
     *
     * @param node
     *            a node
     * @return an optional {@link Node} (or {@link Optional#empty()} if is at the root of the diagram)
     */
    public Optional<Node> getParentNode(Node node) {
        return this.buildDiagramNavigator().getParentNode(node);
    }

    private DiagramNavigator buildDiagramNavigator() {
        return new DiagramNavigator(this.diagramNavigationService, this.diagram, this.converterNodes);
    }

    /**
     * Assert and get an edge matching the following criteria.
     *
     * @param expectedDescriptionName
     *            the name of the view description
     * @param expectedSource
     *            the expected source node
     * @param expectedTarget
     *            the expected target node
     * @return an Edge (or fails)
     */
    public Edge assertGetUniqueFeatureBasedEdge(String expectedDescriptionName, Node expectedSource, Node expectedTarget) {
        Edge matchingEdge = this.getMatchingEdge(Optional.of(expectedDescriptionName), Optional.empty(), Optional.of(expectedSource.getId()), Optional.of(expectedTarget.getId()));
        if (matchingEdge == null) {
            fail("No feature base edge given the matching criteria");
        }

        return matchingEdge;
    }

    /**
     * Assert there is no feature edge with given description name between the given nodes.
     *
     * @param expectedDescriptionName
     *            the named (id) of the {@link org.eclipse.sirius.components.view.NodeDescription}
     * @param expectedSource
     *            the expected source
     * @param expectedTarget
     *            the expected target
     */
    public void assertNoFeatureBasedEdge(String expectedDescriptionName, Node expectedSource, Node expectedTarget) {
        Edge matchingEdge = this.getMatchingEdge(Optional.of(expectedDescriptionName), Optional.empty(), Optional.of(expectedSource.getId()), Optional.of(expectedTarget.getId()));
        if (matchingEdge != null) {
            fail("There is one edge between the source and target node: Expected none");
        }
    }

    /**
     * Assert there is no feature edge on the diagram starting from the given node.
     *
     * @param expectedDescriptionName
     *            the name of the description view
     * @param expectedSource
     *            the expected source node
     */
    public void assertNoFeatureEdgeStartingFrom(String expectedDescriptionName, Node expectedSource) {
        Edge matchingEdge = this.getMatchingEdge(Optional.of(expectedDescriptionName), Optional.empty(), Optional.of(expectedSource.getId()), Optional.empty());
        if (matchingEdge != null) {
            fail("A matching edge has been found whereas none was expected");
        }
    }

    /**
     * Assert and get a unique node in a parent matching the given criteria.
     *
     * @param expectedDescriptionName
     *            the name of the description view
     * @param expectedParent
     *            the expected parent node
     * @param expectedSemanticElement
     *            the expected semantic element
     * @return a Node (or fails)
     */
    public Node assertGetUniqueMatchingNodeIn(String expectedDescriptionName, Node expectedParent, EObject expectedSemanticElement) {
        DiagramNavigator diagramNav = this.buildDiagramNavigator();
        Predicate<Node> parentPredicate = this.buildParentNodePredicate(diagramNav, expectedParent);
        return this.getUniqueMatch(this.buildDiagramNavigator(), this.buildSemanticPredicate(expectedSemanticElement),
                this.buildNodeDescriptionNamePredicate(expectedDescriptionName).and(parentPredicate));
    }

    /**
     * Assert and get a unique node matching the given criteria.
     *
     * @param expectedParent
     *            the expected parent node
     * @param expectedSemanticElement
     *            the expected semantic element
     * @return a node (or fails)
     */
    public Node assertGetUniqueMatchingNode(String expectedDescriptionName, EObject expectedSemanticElement) {
        Predicate<Node> nodePredicate = node -> this.matchDescriptionName(expectedDescriptionName, node);
        return this.getUniqueMatch(this.buildDiagramNavigator(), this.buildSemanticPredicate(expectedSemanticElement), nodePredicate);
    }

    /**
     * Assert and get a unique node given its id.
     *
     * @param expectedNodeId
     *            the expected node id
     * @return a node (or fails)
     */
    public Node assertUniqueNodeById(String expectedNodeId) {
        Predicate<Node> nodePredicate = this.buildNodeIDPredicate(expectedNodeId);
        return this.getUniqueMatch(this.buildDiagramNavigator(), null, nodePredicate);
    }
}
