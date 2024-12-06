/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to help the definition of node creation tool tests.
 * <p>
 * Concrete node creation tool tests can extend this class and reuse {@link #createNode(String, String, Checker)} to
 * invoke the node creation tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class NodeCreationTest extends AbstractPapyrusWebTest {

    private static final String CANNOT_FIND_NODE_MSG = "Cannot find Node container with label ";

    private static final String CHECKER_IS_NULL_ERROR = "checker cannot be null";

    private static final String NODE_CONTAINER_IS_NOT_NODE_ERROR = "Node container should be a Node";

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
    public NodeCreationTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Creates a node on the diagram with the provided {@code nodeCreationTool}.
     *
     * @param nodeCreationTool
     *            the {@link CreationTool} specifying the tool section and name in the palette
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void createTopNode(CreationTool nodeCreationTool, Checker checker) {
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        this.applyNodeCreationTool(this.representationId, nodeCreationTool);
        // Reload the diagram to ensure it contains the create element
        Diagram diagram = this.getDiagram();
        Node createdNode = diagram.getNodes().get(0);
        checker.validateRepresentationElement(createdNode);
    }

    /**
     * Creates a node in {@code parentName} with the provided {@nodeCreationTool}.
     *
     * @param parentName
     *            the label of the graphical container of the node to create
     * @param nodeCreationTool
     *            the {@link CreationTool} specifying the tool section and name in the palette
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void createSubNode(String parentName, CreationTool nodeCreationTool, Checker checker) {
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        IDiagramElement diagramElementHolder = this.findGraphicalElementExcludingContentByLabel(parentName);
        IDiagramElement diagramElementContent = this.findGraphicalElementContentByLabel(parentName);
        assertThat(diagramElementHolder).as(NODE_CONTAINER_IS_NOT_NODE_ERROR).isInstanceOf(Node.class);
        assertThat(diagramElementHolder).as(CANNOT_FIND_NODE_MSG + parentName).isNotNull();

        // Holder and Content might be the same
        if (diagramElementContent == null) {
            diagramElementContent = diagramElementHolder;
        }
        Node parentNodeHolder = (Node) diagramElementHolder;
        Node parentNodeContent = (Node) diagramElementContent;
        int initialNumberOfChild = parentNodeContent.getChildNodes().size();
        List<String> initialBorderNodeIds = parentNodeHolder.getBorderNodes().stream()
                .map(node -> node.getId())
                .toList();

        // int initialNumberOfBorderNodes = parentNode.getBorderNodes().size();
        this.applyNodeCreationTool(diagramElementContent.getId(), nodeCreationTool);
        // Reload the parent element to ensure it contains the created element
        IDiagramElement updatedDiagramElementHolder = this.findGraphicalElementExcludingContentByLabel(parentName);
        IDiagramElement updatedDiagramElementContent = this.findGraphicalElementContentByLabel(parentName);
        assertThat(updatedDiagramElementHolder).as(NODE_CONTAINER_IS_NOT_NODE_ERROR).isInstanceOf(Node.class);
        assertThat(updatedDiagramElementHolder).as(CANNOT_FIND_NODE_MSG + parentName).isNotNull();
        Node updatedNodeElementHolder = (Node) updatedDiagramElementHolder;
        Node updatedNodeElementContent = (Node) updatedDiagramElementContent;

        // Holder and Content might be the same
        if (updatedNodeElementContent == null) {
            updatedNodeElementContent = updatedNodeElementHolder;
        }
        Node createdNode = null;
        if (updatedNodeElementContent.getChildNodes().size() > initialNumberOfChild) {
            // We assume the created element is always added at the end of the getChildNodes list
            createdNode = updatedNodeElementContent.getChildNodes().get(initialNumberOfChild);
        } else if (updatedNodeElementHolder.getBorderNodes().size() > initialBorderNodeIds.size()) {
            createdNode = updatedNodeElementHolder.getBorderNodes().stream()
                    .filter(node -> !initialBorderNodeIds.contains(node.getId()))
                    .findFirst()
                    .orElse(null);
        } else {
            fail(MessageFormat.format("Cannot find the created node after applying the tool {0}|{1}", nodeCreationTool.getToolSection(), nodeCreationTool.getToolName()));
        }
        checker.validateRepresentationElement(createdNode);
    }

    /**
     * Creates a node in the {@code compartmentMapping} of the {@code parentName} node with the provided
     * {@code nodeCreationTool}.
     *
     * @param parentName
     *            the label of the graphical container of the node to create
     * @param compartmentMapping
     *            the mapping of the compartment to create the node into
     * @param nodeCreationTool
     *            the {@link CreationTool} specifying the tool section and name in the palette
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void createSubNodeOnCompartment(String parentName, String compartmentMapping, CreationTool nodeCreationTool, Checker checker) {
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        Node parentCompartmentNode = this.getSubNode(parentName, compartmentMapping);
        IDiagramElement compartmentElement = this.getSubNode(parentName, compartmentMapping);
        assertThat(compartmentElement).as("Cannot find Node compartment with mapping " + compartmentMapping + " in parent " + parentName).isNotNull();
        int compartmentChildCount = parentCompartmentNode.getChildNodes().size();
        this.applyNodeCreationTool(parentCompartmentNode.getId(), nodeCreationTool);
        // Reload the parent element to ensure it contains the created element
        IDiagramElement updatedCompartmentElement = this.getSubNode(parentName, compartmentMapping);
        assertThat(updatedCompartmentElement).as(NODE_CONTAINER_IS_NOT_NODE_ERROR).isInstanceOf(Node.class);
        assertThat(updatedCompartmentElement).as("Cannot find Node compartment with mapping " + compartmentMapping + " in parent " + parentName).isNotNull();
        // We assume the created element is always added at the end of the getChildNodes list
        Node createdNode = ((Node) updatedCompartmentElement).getChildNodes().get(compartmentChildCount);
        checker.validateRepresentationElement(createdNode);
    }
}
