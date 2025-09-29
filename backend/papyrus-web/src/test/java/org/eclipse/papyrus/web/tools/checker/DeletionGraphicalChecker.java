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
package org.eclipse.papyrus.web.tools.checker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to check that a graphical element has been removed from the diagram.
 * <p>
 * It also validates that the diagram contains the appropriate number of elements after the creation.
 * </p>
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class DeletionGraphicalChecker implements Checker {

    /**
     *
     */
    private static final String LESS_ELEMENT = " less element";

    protected Supplier<Diagram> diagramSupplier;

    protected Supplier<IDiagramElement> graphicalOwnerSupplier;

    protected String mappingType;

    protected int diagramAllChildCount;

    protected int diagramDirectChildCount;

    protected int graphicalOwnerChildCount;

    protected int graphicalParentBorderedChildCount;

    /**
     * Initializes the checker with the provided parameters.
     *
     * @param diagramSupplier
     *            the diagram which contain the owner of the node to remove
     * @param graphicalOwnerSupplier
     *            owner node of the element to remove
     */
    public DeletionGraphicalChecker(Supplier<Diagram> diagramSupplier, Supplier<IDiagramElement> graphicalOwnerSupplier) {
        this.diagramSupplier = diagramSupplier;
        this.graphicalOwnerSupplier = graphicalOwnerSupplier;
        Diagram diagram = this.diagramSupplier.get();
        this.diagramAllChildCount = this.getDiagramSize(diagram);
        this.diagramDirectChildCount = diagram.getNodes().size() + diagram.getEdges().size();
        this.graphicalOwnerChildCount = this.getGraphicalElementChildCount(graphicalOwnerSupplier).orElse(this.diagramDirectChildCount);
        this.graphicalParentBorderedChildCount = this.getGraphicalParentBorderedCount(graphicalOwnerSupplier).orElse(0);

    }

    @Override
    public void validateRepresentationElement(IDiagramElement elementToRemove) {

        // 1. check element has been removed from the diagram
        this.checkElementHasBeenRemovedFromDiagram(elementToRemove);

        // 2. check the number of element on diagram
        this.checkNumberOfRemovedElement(elementToRemove);

    }

    protected void checkElementHasBeenRemovedFromDiagram(IDiagramElement element) {
        List<IDiagramElement> removedElement = null;
        if (this.graphicalOwnerSupplier != null && this.graphicalOwnerSupplier.get() instanceof Node graphicalParentNode) {
            removedElement = this.findGraphicalElementFromContainer(graphicalParentNode, element);
        } else {
            removedElement = this.findGraphicalElementFromDiagram(element);
        }
    }

    protected void checkNumberOfRemovedElement(final IDiagramElement element) {
        Diagram diagram = this.diagramSupplier.get();
        int newDiagramAllNodesCount = this.getDiagramSize(diagram);

        assertThat(newDiagramAllNodesCount).as("The diagram should contain " + this.getExpectedNumerOfDeletedElements() + LESS_ELEMENT)
                .isEqualTo(this.diagramAllChildCount - this.getExpectedNumerOfDeletedElements());

        int newGraphicalOwnerChildCount = this.getGraphicalElementChildCount(this.graphicalOwnerSupplier)
                .orElse(this.diagramSupplier.get().getNodes().size() + this.diagramSupplier.get().getEdges().size());

        int newHolderBorderedNodeCount = this.getGraphicalParentBorderedCount(this.graphicalOwnerSupplier).orElse(0);

        if (element instanceof Node && ((Node) element).isBorderNode()) {
            // when there is no Holder, it doesn't work.
            assertThat(newHolderBorderedNodeCount).as("The graphical container should contain " + this.getExpectedNumberOfDeletedGraphicalOwnerDirectChildren() + LESS_ELEMENT)
                    .isEqualTo(this.graphicalParentBorderedChildCount - this.getExpectedNumberOfDeletedGraphicalOwnerDirectChildren());
        } else {
            assertThat(newGraphicalOwnerChildCount)
                    .as("The graphical container should contain " + this.getExpectedNumberOfDeletedGraphicalOwnerDirectChildren() + LESS_ELEMENT)
                    .isEqualTo(this.graphicalOwnerChildCount - this.getExpectedNumberOfDeletedGraphicalOwnerDirectChildren());
        }
    }

    /**
     * The expected number of deleted elements in the diagram.
     * <p>
     * This method is used by {@link #checkNumberOfRemovedElement()} to ensure that the correct number of elements have
     * been deleted in the diagram. The default implementation of this method returns {@code 1}.
     * </p>
     *
     * @return the expected number of deleted elements in the diagram
     */
    protected int getExpectedNumerOfDeletedElements() {
        return 1;
    }

    /**
     * The expected number of deleted element in the checked graphical owner.
     * <p>
     * This method is used by {@link #checkNumberOfRemovedElement()} to ensure that the correct number of elements have
     * been deleted in the checked graphical parent. The default implementation of this method returns {@code 1}.
     * </p>
     * <p>
     * This method may return a number smaller than {@link #getExpectedNumerOfDeletedElements()} if some elements aren't
     * deleted in the graphical owner.
     * </p>
     *
     * @return the expected number of deleted element in the checked graphical parent
     */
    protected int getExpectedNumberOfDeletedGraphicalOwnerDirectChildren() {
        return 1;
    }

    private int getDiagramSize(Diagram diagram) {
        int result = diagram.getEdges().size();
        for (Node node : diagram.getNodes()) {
            result += this.getDiagramSize(node);
        }
        return result;
    }

    private int getDiagramSize(Node node) {
        int result = 1;
        for (Node subNode : node.getChildNodes()) {
            result += this.getDiagramSize(subNode);
        }
        for (Node borderNode : node.getBorderNodes()) {
            result += this.getDiagramSize(borderNode);
        }
        return result;
    }

    private Optional<Integer> getGraphicalElementChildCount(Supplier<IDiagramElement> elementSupplier) {
        Optional<Integer> result = Optional.empty();
        if (elementSupplier != null && elementSupplier.get() instanceof Node graphicalElement) {
            result = Optional.of(graphicalElement.getChildNodes().size() + graphicalElement.getBorderNodes().size());
        }
        return result;
    }

    private List<IDiagramElement> findGraphicalElementFromDiagram(IDiagramElement removedElement) {
        Diagram diagram = this.diagramSupplier.get();
        List<IDiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getNodes()) {
            result.addAll(this.findGraphicalElementFromContainer(node, removedElement));
        }
        for (Edge edge : diagram.getEdges()) {
            // Compare IDs instead of objects, the edge may have been reloaded and can be a different instance.
            if (Objects.equals(edge.getId(), removedElement.getId())) {
                result.add(edge);
            }
        }
        assertThat(result).as("The graphical element  should be removed.").hasSize(0);
        return result;

    }

    private List<IDiagramElement> findGraphicalElementFromContainer(Node node, IDiagramElement removedElement) {
        List<IDiagramElement> result = new ArrayList<>();
        // Compare IDs instead of objects, the node may have been reloaded and can be a different instance.
        if (Objects.equals(node.getId(), removedElement.getId())) {
            result.add(node);
        }
        for (Node childNode : node.getChildNodes()) {
            result.addAll(this.findGraphicalElementFromContainer(childNode, removedElement));
        }
        return result;
    }

    /**
     * Counts the number of direct children of the provided {@code elementSupplier}.
     * <p>
     * This method gets the {@link IDiagramElement} from the provided supplier every time it is called. This ensures
     * that the latest version of the {@link IDiagramElement} is manipulated. This is specially useful for graphical
     * elements that are updated by the test, and need to be reloaded to check their new state.
     *
     * @param elementSupplier
     *            the supplier of {@link IDiagramElement}
     * @return the number of direct children of the provided {@code elementSupplier}
     */
    private Optional<Integer> getGraphicalParentBorderedCount(Supplier<IDiagramElement> elementSupplier) {
        return this.getGraphicalParentBorderedNodes(elementSupplier).map(List::size).map(Optional::of).orElse(Optional.empty());
    }

    /**
     * Returns the direct child nodes of the provided {@code elementSupplier}.
     * <p>
     * This method gets the {@link IDiagramElement} from the provided supplier every time is it called. This ensures
     * that the latest version of the {@link IDiagramElement} is manipulated. This is specially useful for graphical
     * elements that are updated by the test, and need to be reloaded to check their new state.
     *
     * @param elementSupplier
     *            the supplier of {@link IDiagramElement}
     * @return the direct child nodes of the provided {@code elementSupplier}
     */
    private Optional<List<Node>> getGraphicalElementChildNodes(Supplier<IDiagramElement> elementSupplier) {
        Optional<List<Node>> result = Optional.empty();
        if (elementSupplier != null && elementSupplier.get() instanceof Node graphicalElement) {
            List<Node> allChilds = new ArrayList<>(graphicalElement.getChildNodes());
            allChilds.addAll(graphicalElement.getBorderNodes());
            result = Optional.of(allChilds);
        }
        return result;
    }

    /**
     * Returns the direct child nodes of the provided {@code elementSupplier}.
     * <p>
     * This method gets the {@link IDiagramElement} from the provided supplier every time is it called. This ensures
     * that the latest version of the {@link IDiagramElement} is manipulated. This is specially useful for graphical
     * elements that are updated by the test, and need to be reloaded to check their new state.
     *
     * @param elementSupplier
     *            the supplier of {@link IDiagramElement}
     * @return the direct child nodes of the provided {@code elementSupplier}
     */
    private Optional<List<Node>> getGraphicalParentBorderedNodes(Supplier<IDiagramElement> elementSupplier) {
        Optional<List<Node>> result = Optional.empty();
        Diagram diagram = this.diagramSupplier.get();
        if (elementSupplier != null && elementSupplier.get() instanceof Node graphicalElement) {
            Optional<Node> parent = this.getAllNodeAndSubNodes(diagram).stream().filter(node -> node.getTargetObjectId().equals(graphicalElement.getTargetObjectId())).findFirst();
            if (parent.isPresent()) {
                List<Node> allBorderedNodes = new ArrayList<>(parent.get().getBorderNodes());
                result = Optional.of(allBorderedNodes);
            }
        }
        return result;
    }

    private List<Node> getAllNodeAndSubNodes(Diagram diagram) {
        return this.getSubNodes(diagram.getNodes());
    }

    /**
     * @param nodes
     * @return
     */
    private List<Node> getSubNodes(List<Node> nodes) {
        List<Node> result = new ArrayList<>(nodes);
        for (Node node : nodes) {
            for (Node child : node.getChildNodes()) {
                // result.add(child);
                result.addAll(this.getSubNodes(List.of(child)));
            }
            for (Node child : node.getBorderNodes()) {
                // result.add(child);
                result.addAll(this.getSubNodes(List.of(child)));
            }
        }
        return result;
    }

}
