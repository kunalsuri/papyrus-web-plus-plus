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
package org.eclipse.papyrus.web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.tests.utils.UMLTestHelper;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.springframework.data.util.Pair;

/**
 * Helper used to check the behavior of {@link AbstractDiagramService} on a diagram.
 *
 * @author Arthur Daussy
 */
public class DiagramServiceTestHelper {

    private final DiagramTestHelper diagramHelper;

    private final AbstractDiagramService diagramService;

    private final IEditingContext editingContext;

    private final IIdentityService identityService;

    private final IObjectSearchService objectSearchService;

    private final UMLTestHelper umlHelper = new UMLTestHelper();

    public DiagramServiceTestHelper(DiagramTestHelper diagramHelper, AbstractDiagramService diagramService,
            IEditingContext editingContext, IIdentityService identityService,
            IObjectSearchService objectSearchService) {
        super();
        this.diagramHelper = diagramHelper;
        this.diagramService = diagramService;
        this.editingContext = editingContext;
        this.identityService = identityService;
        this.objectSearchService = objectSearchService;
    }

    /**
     * Asserts child and sibling creation in a given parent. This test first creates an element in the parent and then
     * used the created node to test the sibling creation.
     *
     * @param parent
     *            parent not
     * @param type
     *            the type of the element to create
     * @param containmentRef
     *            the containment reference
     * @param nodeDescriptionName
     *            the name of the {@link org.eclipse.sirius.components.view.diagram.NodeDescription}
     */
    public void assertChildAndSiblingCreation(Node parent, EClass type, EReference containmentRef, String nodeDescriptionName) {
        Node labelNode = this.assertChildCreation(parent, type, containmentRef, nodeDescriptionName);

        // Due to Workaround for https://github.com/PapyrusSirius/papyrus-web/issues/164
        // We need to test sibling creation
        this.assertSiblingCreation(labelNode, type, containmentRef, nodeDescriptionName);
    }

    /**
     * Checks the behavior of creation on parent visual node.
     *
     * <p>
     * This code use the
     * {@link AbstractDiagramService#create(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * service to create a new element on a Node. Then check the semantic creation occurred and a new node has
     * been added inside the parent node with given EReference.
     * </p>
     *
     * @param visualParent
     *            the visual parent
     * @param type
     *            the type of the element to be created
     * @param containmentRef
     *            the containment reference
     * @param expectedNodeDescriptionId
     *            the name of the {@link NodeDescription} used to new {@link Node}
     * @return the newly created node
     */
    public Node assertChildCreation(Node visualParent, EClass type, EReference containmentRef,
            String expectedNodeDescriptionId) {
        return this.assertChildCreation(visualParent, type, containmentRef, expectedNodeDescriptionId,
                this.diagramHelper.getSemanticElement(visualParent));
    }

    /**
     * Checks the behavior of creation on parent visual node.
     *
     * <p>
     * This code use the
     * {@link AbstractDiagramService#create(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * service to create a new element on a Node. Then check the semantic creation occurred and a new node has
     * been added inside the parent node with given EReference.
     * </p>
     *
     * @param visualParent
     *            the visual parent
     * @param type
     *            the type of the element to be created
     * @param containmentRef
     *            the containment reference
     * @param expectedNodeDescriptionId
     *            the name of the {@link NodeDescription} used to new {@link Node}
     * @param expectedSemanticOwner
     *            expected semantic owner of the creation
     * @return the newly created node
     */
    public Node assertChildCreation(Node visualParent, EClass type, EReference containmentRef,
            String expectedNodeDescriptionId, EObject expectedSemanticOwner) {
        EObject semanticOwner = this.diagramHelper.getSemanticElement(visualParent);

        return this.assertChildCreation(visualParent, semanticOwner, type, containmentRef, expectedNodeDescriptionId,
                expectedSemanticOwner);
    }

    /**
     * Checks the behavior of creation on parent visual node.
     *
     * <p>
     * This code use the
     * {@link AbstractDiagramService#create(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * service to create a new element on a Node. Then check the semantic creation occurred and a new node has
     * been added inside the parent node with given EReference.
     * </p>
     *
     * @param visualParent
     *            the visual parent
     * @param type
     *            the type of the element to be created
     * @param containementRef
     *            the containment reference
     * @param expectedNodeDescriptionId
     *            the name of the {@link NodeDescription} used to new {@link Node}
     * @param expectedSemanticOwner
     *            expected semantic owner of the creation
     * @return the newly created node
     */
    public Node assertChildCreation(Node visualParent, EObject self, EClass type, EReference containementRef, String expectedNodeDescriptionId, EObject expectedSemanticOwner) {
        EObject newElement = this.diagramHelper.modify(context -> {
            EObject aNewElement = this.diagramService.create(self, type.getName(), containementRef.getName(), visualParent, context, this.diagramHelper.getConvertedNodes());
            assertTrue(type.isInstance(aNewElement));
            assertEquals(expectedSemanticOwner, aNewElement.eContainer());
            if (containementRef.isMany()) {
                assertTrue(((Collection<?>) expectedSemanticOwner.eGet(containementRef)).contains(aNewElement));
            } else {
                assertEquals(expectedSemanticOwner.eGet(containementRef), aNewElement);

            }
            return aNewElement;
        });

        return this.diagramHelper.assertGetUniqueMatchingNodeIn(expectedNodeDescriptionId, visualParent, newElement);
    }

    /**
     * Asserts that an element can be created and dropped inside a given parent.
     *
     * @param parentNode
     *            the parent node
     * @param type
     *            the type of element to create
     * @param containmentReference
     *            the containment {@link EReference}
     * @param nodeDescriptionName
     *            the name of the description node to be created in both case
     *
     * @return the dropped node
     */
    public Node assertChildCreationAndDrop(Node parentNode, java.lang.Class<? extends EObject> type, EReference containmentReference, String nodeDescriptionName) {
        EObject parent = this.diagramHelper.getSemanticElement(parentNode);
        this.assertChildCreation(parentNode, this.umlHelper.getEClass(type), containmentReference, nodeDescriptionName, parent);
        Node droppedNode = this.assertSemanticDrop(this.umlHelper.createIn(type, parent), parentNode, nodeDescriptionName);
        return droppedNode;
    }

    /**
     * Checks the behavior of creation on sibling visual node.
     *
     * <p>
     * This code use the
     * {@link AbstractDiagramService#createSibling(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * service to create a new element as a sibling of on a Node. Then check the the semantic creation occurred and the
     * a new node has been added aside the sibling node with given EReference.
     * </p>
     *
     * @param visualSibling
     *            the visual sibling
     * @param type
     *            the type of the element to be created
     * @param containmentRef
     *            the containment reference
     * @param expectedNodeDescriptionId
     *            the name of the {@link NodeDescription} used to new {@link Node}
     * @param expectedSemanticOwner
     *            expected semantic owner of the creation
     * @return the newly created node
     */
    public Node assertSiblingCreation(Node visualSibling, EClass type, EReference containmentRef,
            String expectedNodeDescriptionId, EObject expectedSemanticOwner) {
        EObject semanticSibling = this.diagramHelper.getSemanticElement(visualSibling);

        EObject newElement = this.diagramHelper.modify(context -> {
            EObject aNewElement = this.diagramService.createSibling(semanticSibling, type.getName(),
                    containmentRef.getName(), visualSibling, context, this.diagramHelper.getConvertedNodes());
            assertTrue(type.isInstance(aNewElement));
            assertEquals(semanticSibling.eContainer(), aNewElement.eContainer());
            if (containmentRef.isMany()) {
                assertTrue(((Collection<?>) expectedSemanticOwner.eGet(containmentRef)).contains(aNewElement));
            } else {
                assertEquals(expectedSemanticOwner.eGet(containmentRef), aNewElement);

            }
            return aNewElement;
        });

        Node parentNode = this.diagramHelper.getParentNode(visualSibling).get();
        return this.diagramHelper.assertGetUniqueMatchingNodeIn(expectedNodeDescriptionId, parentNode, newElement);
    }

    /**
     * Checks the behavior of creation on sibling visual node.
     *
     * <p>
     * This code use the
     * {@link AbstractDiagramService#createSibling(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * service to create a new element as a sibling of on a Node. Then check the semantic creation occurred and the
     * a new node has been added aside the sibling node with given EReference. The expected semantic owner is expected
     * to be the semantic element of the visual parent
     * </p>
     *
     * @param visualSinling
     *            the visual sibling
     * @param type
     *            the type of the element to be created
     * @param containementRef
     *            the containment reference
     * @param expectedNodeDescriptionId
     *            the name of the {@link NodeDescription} used to new {@link Node}
     * @return the newly created node
     */
    protected Node assertSiblingCreation(Node visualSinling, EClass type, EReference containementRef, String expectedNodeDescriptionId) {
        return this.assertSiblingCreation(visualSinling, type, containementRef, expectedNodeDescriptionId, this.diagramHelper.getSemanticElement(visualSinling).eContainer());
    }

    /**
     * Uses the
     * {@link AbstractDiagramService#create(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * and then checks that the creation was properly handled.
     *
     * @param type
     *            the type of the new element being created
     * @param containementRef
     *            the containment reference for the new element
     * @param expectedNodeDescriptionName
     *            the expected NodeDescription of the created element
     * @return a pair of semantic element and node
     */
    public Pair<EObject, Node> assertRootCreation(EClass type, EReference containementRef, String expectedNodeDescriptionName) {
        EObject semanticOwner = this.diagramHelper.getDiagramOwner();

        EObject newElement = this.diagramHelper.modify(context -> {
            EObject aNewElement = this.diagramService.create(semanticOwner, type.getName(), containementRef.getName(), null, context, this.diagramHelper.getConvertedNodes());
            assertTrue(type.isInstance(aNewElement));

            if (containementRef.isMany()) {
                assertTrue(((Collection<?>) semanticOwner.eGet(containementRef)).contains(aNewElement));
            } else {
                assertEquals(semanticOwner.eGet(containementRef), aNewElement);

            }
            return aNewElement;
        });

        Node node = this.diagramHelper.assertGetUniqueRootNode(expectedNodeDescriptionName, newElement);
        return Pair.of(newElement, node);
    }

    /**
     * Uses the {@link AbstractDiagramService#semanticDrop(EObject, Node, IEditingContext, IDiagramContext, Map)} and then checks that the drop was properly handled.
     *
     * @param expectedNodeDescriptionName
     *            the expected NodeDescription of the dropped element
     * @return the new node created after the drop
     */
    public Node assertSemanticDrop(EObject semanticDroppedElement, Node targetView, String expectedNodeDescriptionName) {

        EObject droppedElement = this.diagramHelper.modify(context -> {
            EObject theDropppedElement = this.diagramService.semanticDrop(semanticDroppedElement, targetView, this.editingContext, context, this.diagramHelper.getConvertedNodes());
            return theDropppedElement;
        });

        return this.diagramHelper.assertGetUniqueMatchingNodeIn(expectedNodeDescriptionName, targetView, droppedElement);

    }

    /**
     * Uses the {@link AbstractDiagramService#semanticDrop (...)} and then checks that the drop was properly handled. Here the
     * expected behavior is that the new element is created as a sibling of the target view
     *
     * @param expectedNodeDescriptionName
     *            the expected NodeDescription of the dropped element
     * @return the new node created after the drop
     */
    public Node assertSiblingSemanticDrop(EObject semanticDroppedElement, Node targetView, String expectedNodeDescriptionName) {

        EObject droppedElement = this.diagramHelper.modify(context -> {
            EObject theDropppedElement = this.diagramService.semanticDrop(semanticDroppedElement, targetView, this.editingContext, context, this.diagramHelper.getConvertedNodes());
            return theDropppedElement;
        });

        Node parentView = this.diagramHelper.getParentNode(targetView).get();
        return this.diagramHelper.assertGetUniqueMatchingNodeIn(expectedNodeDescriptionName, parentView, droppedElement);

    }

    /**
     * Asserts the drop behavior of element contained in compartment. On element in a compartment can be:
     * <p>
     * <ul>
     * <li>Dropped inside the parent compartment</li>
     * <li>Dropped onto a sibling</li>
     * </ul>
     *
     * In both case the element view should be displayed in the compartment.
     * </p>
     *
     * @param compartiment
     * @param firstDrop
     * @param secondDrop
     * @param nodeDescriptionName
     */
    public void assertChildAndSiblingSemanticDrop(Node compartiment, EObject firstDrop, EObject secondDrop, String nodeDescriptionName) {
        Node dropppedElement = this.assertSemanticDrop(firstDrop, compartiment, nodeDescriptionName);
        this.assertSiblingSemanticDrop(secondDrop, dropppedElement, nodeDescriptionName);
    }

    /**
     * Asserts a reconnection of target.
     *
     * @param edgeMatchet
     *            an edge matcher to select the edge to reconnect
     * @param oldTargetNode
     *            an ElementMatcher to select the old target node
     * @param newTargetNode
     *            {@link ElementMatcher} to select the new target
     * @param source
     *            the non changing source node
     */
    public void assertTargetReconnection(ElementMatcher edgeMatchet, ElementMatcher oldTargetNode, ElementMatcher newTargetNode, ElementMatcher source) {

        Node oldTargetView = this.diagramHelper.assertGetUniqueMatchingNode(oldTargetNode.getDescriptionId(), oldTargetNode.getSemanticElement());
        Node newTargetView = this.diagramHelper.assertGetUniqueMatchingNode(newTargetNode.getDescriptionId(), newTargetNode.getSemanticElement());
        this.diagramHelper.modify(diagramContext -> {
            this.diagramService.reconnectTargetOnDomainBasedEdge(edgeMatchet.getSemanticElement(), oldTargetNode.getSemanticElement(), newTargetNode.getSemanticElement(), newTargetView, oldTargetView,
                    this.editingContext, diagramContext.getDiagram());
        });

        Node sourceNode = this.diagramHelper.assertGetUniqueMatchingNode(source.getDescriptionId(), source.getSemanticElement());
        Node targetNode = this.diagramHelper.assertGetUniqueMatchingNode(oldTargetNode.getDescriptionId(), oldTargetNode.getSemanticElement());
        Node target2Node = this.diagramHelper.assertGetUniqueMatchingNode(newTargetNode.getDescriptionId(), newTargetNode.getSemanticElement());

        Optional<String> semanticEdgeId = Optional.of(this.identityService.getId(edgeMatchet.getSemanticElement()));

        Edge oldMatchingEdge = this.diagramHelper.getMatchingEdge(Optional.of(edgeMatchet.getDescriptionId()), semanticEdgeId, Optional.of(sourceNode.getId()), Optional.of(targetNode.getId()));

        // No more edge from
        assertNull(oldMatchingEdge);

        Edge newMatchingEdge = this.diagramHelper.getMatchingEdge(Optional.of(edgeMatchet.getDescriptionId()), semanticEdgeId, Optional.of(sourceNode.getId()), Optional.of(target2Node.getId()));

        assertNotNull(newMatchingEdge);

    }

    /**
     * Asserts a reconnection of source.
     *
     * @param semanticEdge
     *            an edge matcher to select the edge to reconnect
     * @param oldSourceNode
     *            an ElementMatcher to select the old source node
     * @param newSourceNode
     *            {@link ElementMatcher} to select the new source
     * @param target
     *            the non changing target node
     */
    public void assertSourceReconnection(ElementMatcher semanticEdge, ElementMatcher oldSourceNode, ElementMatcher newSourceNode, ElementMatcher target) {

        Node oldSourceView = this.diagramHelper.assertGetUniqueMatchingNode(oldSourceNode.getDescriptionId(), oldSourceNode.getSemanticElement());
        Node newSourceView = this.diagramHelper.assertGetUniqueMatchingNode(newSourceNode.getDescriptionId(), newSourceNode.getSemanticElement());
        this.diagramHelper.modify(diagramContext -> {
            this.diagramService.reconnectSourceOnDomainBasedEdge(semanticEdge.getSemanticElement(), oldSourceNode.getSemanticElement(), newSourceNode.getSemanticElement(), newSourceView,
                    oldSourceView, this.editingContext, diagramContext.getDiagram());
        });

        Node sourceNode = this.diagramHelper.assertGetUniqueMatchingNode(oldSourceNode.getDescriptionId(), oldSourceNode.getSemanticElement());
        Node targetNode = this.diagramHelper.assertGetUniqueMatchingNode(target.getDescriptionId(), target.getSemanticElement());
        Node source2Node = this.diagramHelper.assertGetUniqueMatchingNode(newSourceNode.getDescriptionId(), newSourceNode.getSemanticElement());

        Optional<String> semanticEdgeId = Optional.of(this.identityService.getId(semanticEdge.getSemanticElement()));

        Edge oldMatchingEdge = this.diagramHelper.getMatchingEdge(Optional.of(semanticEdge.getDescriptionId()), semanticEdgeId, Optional.of(sourceNode.getId()), Optional.of(targetNode.getId()));

        // No more edge from
        assertNull(oldMatchingEdge);

        Edge newMatchingEdge = this.diagramHelper.getMatchingEdge(Optional.of(semanticEdge.getDescriptionId()), semanticEdgeId, Optional.of(source2Node.getId()), Optional.of(targetNode.getId()));

        assertNotNull(newMatchingEdge);

    }

    /**
     * Checks that a domain based edge is displayed between the given source and target.
     *
     * @param expectedSource
     *            the expected source
     * @param expectedTarget
     *            the expected target
     * @param semanicElementEdge
     *            the semantic element of the edge
     * @param idBuilder
     *            use to compute the ID of a domain based edge
     * @return an Edge or fails
     */
    public Edge checkDisplayedDomainBasedEdge(EObject expectedSource, EObject expectedTarget, EObject semanicElementEdge, IdBuilder idBuilder) {

        return this.buildDomainBasedEdgeTestHelper(idBuilder)//
                .withSource(expectedSource)//
                .withTarget(expectedTarget)//
                .withDomainBasedEdge(semanicElementEdge)//
                .build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();
    }

    /**
     * Pre-build a helper to test creation of domain based edge elements.
     *
     * @param idBuilder
     *            a {@link IdBuilder}used for this diagram
     * @return a SynchronizedDomainBasedEdgeCreationTestHelper.Builder
     */
    public SynchronizedDomainBasedEdgeCreationTestHelper.Builder buildSynchronizedDomainBasedEdgeCreationTestHelper(IdBuilder idBuilder) {
        return SynchronizedDomainBasedEdgeCreationTestHelper.builder()//
                .withDiagramService(this.diagramService)//
                .withEditingContext(this.editingContext)//
                .withObjectSearchService(this.objectSearchService)//
                .withIdBuilder(idBuilder)//
                .withRepresentationHelper(this.diagramHelper);
    }

    /**
     * Pre-build a SynchronizedDomainBasedEdgeTestHelper.
     *
     * @param idBuilder
     *            an {@link IdBuilder}
     * @return SynchronizedDomainBasedEdgeTestHelper.Builder
     */
    public SynchronizedDomainBasedEdgeTestHelper.Builder buildDomainBasedEdgeTestHelper(IdBuilder idBuilder) {
        return SynchronizedDomainBasedEdgeTestHelper.builder()//
                .withIdBuilder(idBuilder)//
                .withIdentityService(this.identityService)//
                .withRepresentationHelper(this.diagramHelper);
    }

}
