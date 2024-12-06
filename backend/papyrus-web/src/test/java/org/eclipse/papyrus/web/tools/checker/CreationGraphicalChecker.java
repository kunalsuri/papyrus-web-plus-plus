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
 * Utility class to check that a graphical element has been created in the diagram.
 * <p>
 * This checker validates that the expected graphical element has been created in the expected graphical parent. It also
 * validates that the diagram contains the appropriate number of elements after the creation.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public abstract class CreationGraphicalChecker implements Checker {

    /**
     *
     */
    private static final String GRAPHICAL_OWNER_ERROR_MSG = "Graphical owner doesn't contain element ";

    /**
     *
     */
    private static final String MORE_ELEMENT = " more element";

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
     *            a supplier to access and reload the diagram
     * @param graphicalOwnerSupplier
     *            a supplier to access and reload the expected graphical owner of the checked element
     * @param mappingType
     *            the expected mapping type of the checked element
     */
    public CreationGraphicalChecker(Supplier<Diagram> diagramSupplier, Supplier<IDiagramElement> graphicalOwnerSupplier, String mappingType) {
        this.diagramSupplier = diagramSupplier;
        this.graphicalOwnerSupplier = graphicalOwnerSupplier;
        this.mappingType = mappingType;
        Diagram diagram = this.diagramSupplier.get();
        this.diagramAllChildCount = this.getDiagramSize(diagram);
        this.diagramDirectChildCount = diagram.getNodes().size() + diagram.getEdges().size();
        this.graphicalOwnerChildCount = this.getGraphicalElementChildCount(graphicalOwnerSupplier).orElse(this.diagramDirectChildCount);
        this.graphicalParentBorderedChildCount = this.getGraphicalParentBorderedCount(graphicalOwnerSupplier).orElse(0);
    }

    @Override
    public void validateRepresentationElement(IDiagramElement element) {

        // 1. check the type of
        this.checkCreatedElementInstanceOf(element);

        // 2. check the mapping type of the node
        this.checkCreatedElementMapping(element);

        // 3. check the number of created element
        this.checkNumberOfCreatedElement(element);

    }

    protected abstract void checkCreatedElementInstanceOf(IDiagramElement element);

    protected abstract void checkCreatedElementMapping(IDiagramElement element);

    protected void checkNumberOfCreatedElement(final IDiagramElement element) {
        Diagram diagram = this.diagramSupplier.get();
        int newDiagramAllNodesCount = this.getDiagramSize(diagram);
        // count diagram elements
        assertThat(newDiagramAllNodesCount).as("The diagram should contain " + this.getExpectedNumberOfCreatedElements() + MORE_ELEMENT)
                .isEqualTo(this.diagramAllChildCount + this.getExpectedNumberOfCreatedElements());

        int newGraphicalOwnerChildCount = this.getGraphicalElementChildCount(this.graphicalOwnerSupplier)
                .orElse(this.diagramSupplier.get().getNodes().size() + this.diagramSupplier.get().getEdges().size());

        int newHolderBorderedNodeCount = this.getGraphicalParentBorderedCount(this.graphicalOwnerSupplier).orElse(0);
        // count nodes child
        if (element instanceof Node && ((Node) element).isBorderNode()) {
            // when there is no Holder, it doesn't work.
            assertThat(newHolderBorderedNodeCount).as("The graphical container should contain " + this.getExpectedNumberOfCreatedElements() + MORE_ELEMENT)
                    .isEqualTo(this.graphicalParentBorderedChildCount + this.getExpectedNumberOfGraphicalOwnerDirectChildren());
        } else {
            assertThat(newGraphicalOwnerChildCount + newHolderBorderedNodeCount).as("The graphical container should contain " + this.getExpectedNumberOfCreatedElements() + MORE_ELEMENT)
                    .isEqualTo(this.graphicalOwnerChildCount
                            // + this.graphicalParentBorderedChildCount
                            + this.getExpectedNumberOfGraphicalOwnerDirectChildren());
        }
        if (element instanceof Node node) {
            List<Node> graphicalOwnerChildren = this.getGraphicalElementChildNodes(this.graphicalOwnerSupplier).orElse(this.diagramSupplier.get().getNodes());
            // if (((Node) element).isBorderNode()) {
            // List<Node> graphicalParentBorderedNode =
            // this.getGraphicalParentBorderedNodes(this.graphicalOwnerSupplier).orElse(this.diagramSupplier.get().getNodes());
            // assertThat(graphicalParentBorderedNode.stream().map(Node::getId)).as(GRAPHICAL_OWNER_ERROR_MSG +
            // element.getId())
            // .anyMatch(childId -> Objects.equals(childId, element.getId()));
            // } else
            if (graphicalOwnerChildren.isEmpty()) {
                List<Node> graphicalParentBorderedNode = this.getGraphicalParentBorderedNodes(this.graphicalOwnerSupplier).orElse(this.diagramSupplier.get().getNodes());
                assertThat(graphicalParentBorderedNode.stream().map(Node::getId)).as(GRAPHICAL_OWNER_ERROR_MSG + element.getId())
                        .anyMatch(childId -> Objects.equals(childId, element.getId()));

            } else {
                assertThat(graphicalOwnerChildren.stream().map(Node::getId)).as(GRAPHICAL_OWNER_ERROR_MSG + element.getId())
                        .anyMatch(childId -> Objects.equals(childId, element.getId()));
            }
        } else if (element instanceof Edge edge) {
            List<Edge> edges = this.diagramSupplier.get().getEdges();
            assertThat(edges.stream().map(Edge::getId)).as("Diagram owner doesn't contain element " + element.getId()).anyMatch(edgeId -> Objects.equals(edgeId, element.getId()));
        }
    }

    /**
     * The expected number of created elements in the diagram.
     * <p>
     * This method is used by {@link #checkNumberOfCreatedElement(IDiagramElement)} to ensure that the correct number of
     * elements have been created in the diagram. The default implementation of this method returns {@code 1}.
     * </p>
     *
     * @return the expected number of created elements in the diagram
     */
    protected int getExpectedNumberOfCreatedElements() {
        return 1;
    }

    /**
     * The expected number of created element in the checked graphical owner.
     * <p>
     * This method is used by {@link #checkNumberOfCreatedElement(IDiagramElement)} to ensure that the correct number of
     * elements have been created in the checked graphical parent. The default implementation of this method returns
     * {@code 1}.
     * </p>
     * <p>
     * This method may return a number smaller than {@link #getExpectedNumberOfCreatedElements()} if some elements
     * aren't created in the graphical owner.
     * </p>
     *
     * @return the expected number of created element in the checked graphical parent
     *
     * @return
     */
    protected int getExpectedNumberOfGraphicalOwnerDirectChildren() {
        return 1;
    }

    /**
     * Returns the total number of elements contained in the provided {@code diagram}.
     *
     * @param diagram
     *            the diagram to compute the size of
     * @return the total number of elements contained in the provided {@code diagram}
     * @see #getDiagramSize(Node)
     */
    private int getDiagramSize(Diagram diagram) {
        int result = diagram.getEdges().size();
        for (Node node : diagram.getNodes()) {
            result += this.getDiagramSize(node);
        }
        return result;
    }

    /**
     * Returns the number of nodes in the provided {@code node}'s sub-tree of elements.
     *
     * @param node
     *            the node to count the sub-tree elements from
     * @return the number of nodes in the provided {@code node}'s sub-tree of elements.
     */
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
    private Optional<Integer> getGraphicalElementChildCount(Supplier<IDiagramElement> elementSupplier) {
        Optional<Integer> result = Optional.empty();
        if (elementSupplier != null && elementSupplier.get() instanceof Node graphicalElement) {
            result = Optional.of(graphicalElement.getChildNodes().size() + graphicalElement.getBorderNodes().size());
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
