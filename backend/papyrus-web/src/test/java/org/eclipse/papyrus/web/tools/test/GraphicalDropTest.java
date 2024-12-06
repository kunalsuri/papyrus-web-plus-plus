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
package org.eclipse.papyrus.web.tools.test;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to help the definition of graphical drop tool tests.
 * <p>
 * Concrete graphical drop tool tests can extend this class and reuse
 * {@link #graphicalDropOnContainer(Node, String, Checker)} and {@link #graphicalDropOnDiagram(Node, Checker)} to invoke
 * the graphical drop tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class GraphicalDropTest extends AbstractPapyrusWebTest {

    /**
     * The suffix to use to create elements to drop.
     */
    protected static final String DROP_SUFFIX = "Drop";

    /**
     * Initializes the test with the provided {@code representationName} and {@code rootElementEClass}.
     *
     * @param documentName
     *            the name of the document to create
     * @param representationName
     *            the name of the representation to create
     * @param rootElementEClass
     *            the type of the root semantic element to create
     *
     * @see AbstractPapyrusWebTest#AbstractPapyrusWebTest(String, String, EClass)
     */
    public GraphicalDropTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Drops the provided {@code nodeToDrop} on the diagram.
     *
     * @param nodeToDrop
     *            the graphical {@link Node} to drop
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    public void graphicalDropOnDiagram(Node nodeToDrop, Checker checker) {
        List<String> initialDiagramNodeIds = this.getDiagram().getNodes().stream().map(Node::getId).toList();
        this.applyDropNodeTool(nodeToDrop.getId(), this.representationId);
        Node droppedNode = this.getDiagram().getNodes().stream()
                .filter(node -> !initialDiagramNodeIds.contains(node.getId()))
                .findFirst()
                .orElse(null);
        checker.validateRepresentationElement(droppedNode);
    }

    /**
     * Drops the provided {@code nodeToDrop} on the provided {@code targetNodeLabel}.
     *
     * @param nodeToDrop
     *            the graphical {@link Node} to drop
     * @param targetNodeLabel
     *            the label of the graphical element to drop onto
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    public void graphicalDropOnContainer(Node nodeToDrop, String targetNodeLabel, Checker checker) {
        Node targetNode = (Node) this.findGraphicalElementContentByLabel(targetNodeLabel);
        List<String> initialTargetNodeChildrenIds = targetNode.getChildNodes().stream().map(Node::getId).toList();
        this.applyDropNodeTool(nodeToDrop.getId(), targetNode.getId());
        Node updatedTargetNode = (Node) this.findGraphicalElementContentByLabel(targetNodeLabel);
        Node droppedNode = updatedTargetNode.getChildNodes().stream()
                .filter(node -> !initialTargetNodeChildrenIds.contains(node.getId()))
                .findFirst()
                .orElse(null);
        checker.validateRepresentationElement(droppedNode);
    }

    /**
     * Drops the provided {@code nodeToDrop} on the provided {@code compartmentName} compartment of the given
     * {@code targetNodeLabel}.
     *
     * @param nodeToDrop
     *            the graphical {@link Node} to drop
     * @param targetNodeLabel
     *            the label of the graphical element to drop onto
     * @param compartmentName
     *            the mapping of the compartment to drop onto
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    public void graphicalDropOnContainerCompartment(Node nodeToDrop, String targetNodeLabel, String compartmentName, Checker checker) {
        Node targetNode = (Node) this.findGraphicalElementExcludingContentByLabel(targetNodeLabel);
        Node targetCompartmentNode = this.getSubNode(targetNode, compartmentName);
        List<String> initialTargetCompartmentNodeChildrenIds = targetCompartmentNode.getChildNodes().stream().map(Node::getId).toList();
        this.applyDropNodeTool(nodeToDrop.getId(), targetCompartmentNode.getId());
        Node updatedTargetNode = (Node) this.findGraphicalElementExcludingContentByLabel(targetNodeLabel);
        Node updatedTargetCompartmentNode = this.getSubNode(updatedTargetNode, compartmentName);
        Node droppedNode = updatedTargetCompartmentNode.getChildNodes().stream()
                .filter(node -> !initialTargetCompartmentNodeChildrenIds.contains(node.getId()))
                .findFirst()
                .orElse(null);
        checker.validateRepresentationElement(droppedNode);
    }

}
