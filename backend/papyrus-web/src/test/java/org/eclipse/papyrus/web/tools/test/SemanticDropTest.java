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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeSourceGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeTargetGraphicalChecker;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to help the definition of semantic drop tool tests.
 * <p>
 * Concrete semantic drop tool tests can extend this class and reuse
 * {@link #semanticDropOnContent(String, String, Checker)} to invoke the semantic drop tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class SemanticDropTest extends AbstractPapyrusWebTest {

    /**
     * The label suffix used for the identify the source of dropped {@link Edge}.
     */
    protected static final String SOURCE = "Source";

    /**
     * The label suffix used for the identify the target of dropped {@link Edge}.
     */
    protected static final String TARGET = "Target";

    /**
     * The container label used to specify that an element is contained in the diagram.
     */
    protected static final String DIAGRAM_LABEL = "Diagram";

    private static final String PARENT_ELEMENT_LABEL_CANNOT_BE_NULL = "parentElementLabel cannot be null";

    private static final String DROPPED_ELEMENT_IS_NULL_ERROR = "droppedElementId cannot be null";

    private static final String CHECKER_IS_NULL_ERROR = "checker cannot be null";

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
    public SemanticDropTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Drops the provided {@code droppedElementId} on the diagram.
     *
     * @param droppedElementId
     *            the semantic identifier of the element to drop on the diagram
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void semanticDropOnDiagram(String droppedElementId, Checker checker) {
        assertThat(droppedElementId).as(DROPPED_ELEMENT_IS_NULL_ERROR).isNotNull();
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        String targetElementId = this.getDiagram().getId();
        List<String> droppedElementUUIDs = List.of(droppedElementId);
        int diagramChildCount = this.getDiagram().getNodes().size();
        this.applyDropOnDiagramTool(targetElementId, droppedElementUUIDs);
        checker.validateRepresentationElement(this.getDiagram().getNodes().get(diagramChildCount));
    }

    /**
     * Drops the provided {@link Edge} on the diagram.
     * <p>
     * This method first creates the source and target {@link Node}s and the {@link Edge} with the given
     * {@link CreationTool}s. It then graphically deletes the source {@link Node}, target {@link Node}, and the
     * {@link Edge}. This process ensures that the created {@link Edge} is configured the same way a user-created
     * {@link Edge} would be. The semantic {@link Edge} is then dropped on the diagram, and the result is validated.
     * </p>
     *
     * @param sourceCreationTool
     *            the creation tool for the source {@link Node} of the {@link Edge} to drop
     * @param targetCreationTool
     *            the creation tool for the target {@link Node} of the {@link Edge} to drop
     * @param edgeCreationTool
     *            the creation tool used to initialize the {@link Edge}
     * @param expectedMappingType
     *            the expected mapping type of the dropped {@link Edge}
     *
     * @see #edgeSemanticDropOnContainers(CreationTool, String, CreationTool, String, CreationTool, String, String) to
     *      test the semantic drag & drop of an edge on containers
     */
    protected void edgeSemanticDropOnDiagram(CreationTool sourceCreationTool, CreationTool targetCreationTool, CreationTool edgeCreationTool, String expectedMappingType) {
        this.edgeSemanticDropOnContainers(sourceCreationTool, DIAGRAM_LABEL, targetCreationTool, DIAGRAM_LABEL, edgeCreationTool, DIAGRAM_LABEL, expectedMappingType);
    }

    /**
     * Drops the provided {@link Edge} on the provided {@code edgeContainerLabel}.
     * <p>
     * This method first creates the source and target {@link Node}s and the {@link Edge} with the given
     * {@link CreationTool}s in the given containers. It then graphically deletes the source {@link Node}, target
     * {@link Node}, and the {@link Edge}. This process ensures that the created {@link Edge} is configured the same way
     * a user-created {@link Edge} would be. The semantic {@link Edge} is then dropped on the diagram, and the result is
     * validated.
     * </p>
     * <p>
     * This method can be configured to check the graphical creation of the source/target {@link Node}s in their
     * respective containers. This is configured with the {@code sourceContainerLabel} and {@code targetContainerLabel}
     * arguments. Note that these arguments and {@code edgeContainerLabel} can be set to
     * {@link SemanticDropTest#DIAGRAM_LABEL} to check that the container is the diagram itself.
     * </p>
     *
     * @param sourceCreationTool
     *            the creation tool for the source {@link Node} of the {@link Edge} to drop
     * @param sourceContainerLabel
     *            the label of the container of the source {@link Node}
     * @param targetCreationTool
     *            the creation tool for the target {@link Node} of the {@link Edge} to drop
     * @param targetContainerLabel
     *            the label of the container of the target {@link Node}
     * @param edgeCreationTool
     *            the creation tool used to initialize the {@link Edge}
     * @param edgeContainerLabel
     *            the label of the container of the {@link Edge}
     * @param expectedMappingType
     *            the expected mapping type of the dropped {@link Edge}
     *
     * @see #edgeSemanticDropOnDiagram(CreationTool, CreationTool, CreationTool, String) to test the semantic drag &
     *      drop of an edge on the diagram
     */
    protected void edgeSemanticDropOnContainers(CreationTool sourceCreationTool, String sourceContainerLabel, CreationTool targetCreationTool, String targetContainerLabel,
            CreationTool edgeCreationTool, String edgeContainerLabel, String expectedMappingType) {

        String sourceLabel = sourceCreationTool.getToolEClass().getName() + SOURCE;
        String targetLabel = targetCreationTool.getToolEClass().getName() + TARGET;
        this.createNodeInParentWithLabel(sourceContainerLabel, sourceCreationTool, sourceLabel);
        this.createNodeInParentWithLabel(targetContainerLabel, targetCreationTool, targetLabel);
        EdgeCreationGraphicalChecker edgeChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, expectedMappingType, this.getCapturedEdges());
        EdgeSourceGraphicalChecker sourceChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(sourceLabel));
        EdgeTargetGraphicalChecker targetChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(targetLabel));
        Checker combinedChecker = new CombinedChecker(edgeChecker, sourceChecker, targetChecker);

        this.createEdge(sourceLabel, targetLabel, edgeCreationTool);
        assertEquals(1, this.getDiagram().getEdges().size());
        int initialNumberOfChildrenInSourceContainer = this.getChildCount(sourceContainerLabel);
        int initialNumberOfChildrenInTargetContainer = this.getChildCount(targetContainerLabel);
        Edge edge = this.getDiagram().getEdges().get(0);
        Node sourceNode = (Node) this.findGraphicalElementExcludingContentByLabel(sourceLabel);
        Node targetNode = (Node) this.findGraphicalElementExcludingContentByLabel(targetLabel);
        this.applyNodeGraphicalDeletionTool(sourceNode.getId());
        this.applyNodeGraphicalDeletionTool(targetNode.getId());
        // Ensure the nodes and the edge have been removed from the diagram
        assertTrue(this.getDiagram().getEdges().isEmpty());
        // Reload the containers get the updated lists of children
        if (sourceContainerLabel.equals(targetContainerLabel)) {
            assertEquals(initialNumberOfChildrenInSourceContainer - 2, this.getChildCount(sourceContainerLabel));
        } else {
            assertEquals(initialNumberOfChildrenInSourceContainer - 1, this.getChildCount(sourceContainerLabel));
            assertEquals(initialNumberOfChildrenInTargetContainer - 1, this.getChildCount(targetContainerLabel));
        }
        // Drop the semantic Edge on its container
        String edgeContainerId;
        if (DIAGRAM_LABEL.equals(edgeContainerLabel)) {
            edgeContainerId = this.representationId;
        } else {
            Node edgeContainer = (Node) this.findGraphicalContentIfExistByLabel(edgeContainerLabel);
            edgeContainerId = edgeContainer.getId();
        }

        this.applyDropOnDiagramTool(edgeContainerId, List.of(edge.getTargetObjectId()));
        // Ensure the nodes and the edge have been re-created on the diagram
        assertEquals(1, this.getDiagram().getEdges().size());
        // Reload the containers to get the updated lists of children
        assertEquals(initialNumberOfChildrenInSourceContainer, this.getChildCount(sourceContainerLabel));
        assertEquals(initialNumberOfChildrenInTargetContainer, this.getChildCount(targetContainerLabel));
        // Validate the created Edge
        combinedChecker.validateRepresentationElement(this.getDiagram().getEdges().get(0));
    }

    private void createNodeInParentWithLabel(String parentLabel, CreationTool nodeCreationTool, String nodeLabel) {
        if (DIAGRAM_LABEL.equals(parentLabel)) {
            this.createNodeWithLabel(this.representationId, nodeCreationTool, nodeLabel);
        } else {
            Node sourceContainerNode = (Node) this.findGraphicalContentIfExistByLabel(parentLabel);
            this.createNodeWithLabel(sourceContainerNode.getId(), nodeCreationTool, nodeLabel);
        }
    }

    /**
     * Returns the number of children directly contained by the {@code label} element.
     * <p>
     * This method accepts {@link #DIAGRAM_LABEL} as a parameter, and returns the number of direct children of the
     * diagram.
     * </p>
     *
     * @param label
     *            the label of the graphical element to count the children of
     * @return the number of children
     */
    private int getChildCount(String label) {
        if (DIAGRAM_LABEL.equals(label)) {
            return this.getDiagram().getNodes().size();
        } else {
            Node nodeHolder = (Node) this.findGraphicalElementExcludingContentByLabel(label);
            Node nodeContent = (Node) this.findGraphicalElementContentByLabel(label);
            return nodeContent.getChildNodes().size() + nodeHolder.getBorderNodes().size();
        }
    }

    /**
     * Drops the provided {@code droppedElementId} on the provided {@code parentElementLabel}.
     *
     * @param parentElementLabel
     *            the label of the graphical element to drop onto
     * @param droppedElementId
     *            the semantic identifier of the element to drop
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void semanticDropOnContent(String parentElementLabel, String droppedElementId, Checker checker) {
        assertThat(parentElementLabel).as(PARENT_ELEMENT_LABEL_CANNOT_BE_NULL).isNotNull();
        assertThat(droppedElementId).as(DROPPED_ELEMENT_IS_NULL_ERROR).isNotNull();
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        Node parentGraphicalElement = (Node) this.findGraphicalElementExcludingContentByLabel(parentElementLabel);
        Node parentContentGraphicalElement = (Node) this.findGraphicalElementContentByLabel(parentElementLabel);
        int parentBorderNodeCount = parentGraphicalElement.getBorderNodes().size();
        if (parentContentGraphicalElement == null) {
            parentContentGraphicalElement = parentGraphicalElement;
        }
        String targetElementId = parentContentGraphicalElement.getId();
        int parentChildCount = parentContentGraphicalElement.getChildNodes().size();
        List<String> droppedElementUUIDs = List.of(droppedElementId);

        this.applyDropOnDiagramTool(targetElementId, droppedElementUUIDs);
        Node parentNode = (Node) this.findGraphicalElementExcludingContentByLabel(parentElementLabel);
        Node parentContentNode = (Node) this.findGraphicalElementContentByLabel(parentElementLabel);
        if (parentContentNode == null) {
            parentContentNode = parentNode;
        }
        Node createdNode = null;
        if (parentContentNode.getChildNodes().size() > parentChildCount) {
            createdNode = parentContentNode.getChildNodes().get(parentChildCount);
        } else if (parentNode.getBorderNodes().size() > parentBorderNodeCount) {
            createdNode = parentNode.getBorderNodes().get(parentBorderNodeCount);
        } else {
            fail(MessageFormat.format("Cannot find the created node after the semantic drag & drop of {0} in {1}", droppedElementId, parentElementLabel));
        }
        checker.validateRepresentationElement(createdNode);
    }

    /**
     * Drops the provided {@code droppedElementId} on the provided {@code parentElementLabel}.
     *
     * @param parentElementLabel
     *            the label of the graphical element to drop onto
     * @param droppedElementId
     *            the semantic identifier of the element to drop
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void semanticDropOnHolder(String parentElementLabel, String droppedElementId, Checker checker) {
        assertThat(parentElementLabel).as(PARENT_ELEMENT_LABEL_CANNOT_BE_NULL).isNotNull();
        assertThat(droppedElementId).as(DROPPED_ELEMENT_IS_NULL_ERROR).isNotNull();
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        Node parentHolderGraphicalElement = (Node) this.findGraphicalElementExcludingContentByLabel(parentElementLabel);
        int parentBorderNodeCount = parentHolderGraphicalElement.getBorderNodes().size();
        String targetElementId = parentHolderGraphicalElement.getId();
        int parentChildCount = parentHolderGraphicalElement.getChildNodes().size();
        List<String> droppedElementUUIDs = List.of(droppedElementId);
        this.applyDropOnDiagramTool(targetElementId, droppedElementUUIDs);
        Node parentHolderNode = (Node) this.findGraphicalElementExcludingContentByLabel(parentElementLabel);

        Node createdNode = null;
        if (parentHolderNode.getChildNodes().size() > parentChildCount) {
            createdNode = parentHolderNode.getChildNodes().get(parentChildCount);
        } else if (parentHolderNode.getBorderNodes().size() > parentBorderNodeCount) {
            createdNode = parentHolderNode.getBorderNodes().get(parentBorderNodeCount);
        } else {
            fail(MessageFormat.format("Cannot find the created node after the semantic drag & drop of {0} in {1}", droppedElementId, parentElementLabel));
        }
        checker.validateRepresentationElement(createdNode);
    }

    /**
     * Drops the provided {@code droppedElementId} on the provided {@code parentName}'s {@code compartmentMapping}
     * compartment.
     *
     * @param parentElementLabel
     *            the label of the graphical element to drop onto
     * @param compartmentMapping
     *            the mapping of the compartment to drop the element into
     * @param droppedElementId
     *            the semantic identifier of the element to drop
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void semanticDropOnContentCompartment(String parentElementLabel, String compartmentMapping, String droppedElementId, Checker checker) {
        assertThat(parentElementLabel).as(PARENT_ELEMENT_LABEL_CANNOT_BE_NULL).isNotNull();
        assertThat(droppedElementId).as(DROPPED_ELEMENT_IS_NULL_ERROR).isNotNull();
        assertThat(checker).as(CHECKER_IS_NULL_ERROR).isNotNull();
        Node parentCompartmentNode = this.getSubNode(parentElementLabel, compartmentMapping);
        String targetCompartmentId = parentCompartmentNode.getId();
        int compartmentChildCount = parentCompartmentNode.getChildNodes().size();
        List<String> droppedElementUUIDs = List.of(droppedElementId);
        this.applyDropOnDiagramTool(targetCompartmentId, droppedElementUUIDs);
        Node updatedParentCompartmentNode = this.getSubNode(parentElementLabel, compartmentMapping);
        checker.validateRepresentationElement(updatedParentCompartmentNode.getChildNodes().get(compartmentChildCount));
    }

}
